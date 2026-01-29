// 1. "use client": 이 파일이 브라우저(클라이언트) 환경에서 실행됨을 명시합니다.
"use client";

// --- [라이브러리 및 컴포넌트 임포트] ---
import {
  Map, 
  MapMarker, 
  MarkerClusterer, 
  Roadview, 
} from "react-kakao-maps-sdk";
import { useEffect, useState, useMemo, useRef } from "react"; 
import { useRouter } from "next/navigation"; 
import { hospitalService, userService } from "@/api/services"; 
import {
  Search,
  MapPin,
  Loader2,
  Plus,
  Map as MapIcon,
  ArrowRight,
  Camera,
  X,
  Heart,
  LayoutGrid,
  ChevronRight,
} from "lucide-react";
import makerImg from "../../../public/images/mapMaker.png";

// 🔥 [추가] AI 증상 상담소 컴포넌트 임포트
import AiHospitalSearch from "@/components/features/AiHospitalSearch";

// --- [UI 컴포넌트: 병원 리스트 스켈레톤] ---
const HospitalListSkeleton = () => (
  <div className="bg-white rounded-[2.5rem] p-8 border border-slate-100 shadow-sm animate-pulse mb-6">
    <div className="flex justify-between items-start mb-4">
      <div className="flex items-center gap-2">
        <div className="w-16 h-6 bg-slate-200 rounded-md" />
        <div className="w-9 h-9 bg-slate-200 rounded-full ml-2" />
      </div>
      <div className="w-11 h-11 bg-slate-200 rounded-2xl" />
    </div>
    <div className="h-8 bg-slate-200 rounded w-3/4 mb-4" />
    <div className="h-4 bg-slate-200 rounded w-full mb-2" />
    <div className="h-4 bg-slate-200 rounded w-2/3 mb-8" />
    <div className="h-10 bg-slate-200 rounded-2xl w-full" />
  </div>
);

// --- [메인 페이지 컴포넌트] ---
export default function Page() {
  const router = useRouter(); 

  const isMounted = useRef(true);
  useEffect(() => {
    isMounted.current = true;
    return () => { isMounted.current = false; }; 
  }, []);

  // ✅ [수정] 지도가 이미 로드되어 있는지 확인하는 로직 강화 (무한 로딩 방지 핵심)
  const [isMapReady, setIsMapReady] = useState(false);
  const [map, setMap] = useState<kakao.maps.Map | null>(null); 
  const [mapCenter, setMapCenter] = useState({ lat: 36.3504, lng: 127.3845 });

  // --- [상태 관리] ---
  const [hospitals, setHospitals] = useState<any[]>([]); 
  const [filteredHospitals, setFilteredHospitals] = useState<any[]>([]); 
  const [selectedId, setSelectedId] = useState<number | null>(null); 
  const [visibleCount, setVisibleCount] = useState(6); 
  const [filterCategory, setFilterCategory] = useState("전체"); 
  const [dataLoading, setDataLoading] = useState(true); 
  const [keyword, setKeyword] = useState(""); 

  const [isRoadviewOpen, setIsRoadviewOpen] = useState(false);
  const [roadviewPos, setRoadviewPos] = useState({ lat: 0, lng: 0 });

  // ✅ [수정] 네비게이션 시 이미 로드된 kakao 객체를 즉시 인식하도록 함
  useEffect(() => {
    const checkMapStatus = () => {
      // layout.tsx에서 로드한 kakao 객체가 이미 존재하는지 확인
      if (window.kakao && window.kakao.maps) {
        window.kakao.maps.load(() => {
          if (isMounted.current) setIsMapReady(true);
        });
      }
    };

    checkMapStatus(); // 마운트 시 즉시 확인
    const timer = setInterval(checkMapStatus, 1000); // 1초마다 반복 확인 (백업)
    return () => clearInterval(timer);
  }, []);

  // --- [스크롤 설정] ---
  useEffect(() => {
    const wrapElement = document.querySelector(".wrap") as HTMLElement;
    if (wrapElement) wrapElement.style.overflow = "visible";
    return () => {
      if (wrapElement) wrapElement.style.overflow = "hidden";
    };
  }, []);

  // --- [데이터 로드 및 지오코딩 (핵심 로직)] ---
  useEffect(() => {
    const fetchAndGeocodeHospitals = async () => {
      setDataLoading(true); 
      try {
        const [hospitalsRes, favoritesRes] = await Promise.allSettled([
          hospitalService.getHospitals(),
          userService.getFavorites(),
        ]);

        let dbData: any[] = [];
        const myFavoriteIds = new Set<number>();

        if (hospitalsRes.status === "fulfilled") {
          dbData = hospitalsRes.value.data;
        }
        if (favoritesRes.status === "fulfilled") {
          const favoriteList = favoritesRes.value.data;
          if (Array.isArray(favoriteList)) {
            favoriteList.forEach((item: any) => myFavoriteIds.add(item.id));
          }
        }

        const initialList = dbData.map(item => ({...item, isFavorite: myFavoriteIds.has(item.id)}));
        
        if (isMounted.current) {
          setHospitals(initialList);
          setFilteredHospitals(initialList);
        }

        // ✅ [수정] 지도가 준비되었을 때만 지오코딩 수행
        if (isMapReady && window.kakao?.maps?.services) {
            const geocoder = new window.kakao.maps.services.Geocoder();
            const updatedHospitals = [...initialList];

            for (let i = 0; i < updatedHospitals.length; i++) {
              if (!isMounted.current) break;

              const item = updatedHospitals[i];
              await new Promise((resolve) => {
                geocoder.addressSearch(item.address, (result, status) => {
                  if (status === window.kakao.maps.services.Status.OK && isMounted.current) {
                    updatedHospitals[i] = {
                      ...item,
                      lat: Number(result[0].y),
                      lng: Number(result[0].x),
                    };
                  }
                  setTimeout(resolve, 50); 
                });
              });
              
              if (i % 5 === 0 && isMounted.current) {
                setFilteredHospitals([...updatedHospitals]);
              }
            }
            
            if (isMounted.current) {
              setHospitals(updatedHospitals);
              setFilteredHospitals(updatedHospitals);
            }
        }
      } catch (err) {
        console.error("데이터 로드 실패:", err);
      } finally {
        if (isMounted.current) setDataLoading(false); 
      }
    };

    fetchAndGeocodeHospitals();
  }, [isMapReady]); // ✅ 지도가 준비되면 데이터와 좌표를 맞춤

  // --- [나머지 필터링 및 핸들러 로직 (기존과 100% 동일)] ---
  const categories = useMemo(() => {
    const sets = new Set(hospitals.map((h) => h.treatCategory));
    return ["전체", ...Array.from(sets)];
  }, [hospitals]);

  useEffect(() => {
    let result = hospitals;
    if (filterCategory !== "전체") {
      result = result.filter((h) => h.treatCategory === filterCategory);
    }
    const trimmedKeyword = keyword.trim();
    if (trimmedKeyword !== "") {
      const searchTerms = trimmedKeyword.split(/\s+/); 
      result = result.filter((h) => {
        const name = (h.name || "").toString().toLowerCase();
        const address = (h.address || "").toString().toLowerCase();
        return searchTerms.every((term) => {
          const t = term.toLowerCase();
          return name.includes(t) || address.includes(t);
        });
      });
    }
    setFilteredHospitals(result);
  }, [hospitals, filterCategory, keyword]);

  const toggleFavorite = async (e: React.MouseEvent, id: number) => {
    e.stopPropagation(); 
    const previousHospitals = [...hospitals]; 
    const updateList = (list: any[]) => list.map((item) => item.id === id ? { ...item, isFavorite: !item.isFavorite } : item);
    setHospitals((prev) => updateList(prev));
    setFilteredHospitals((prev) => updateList(prev));
    try {
      await hospitalService.toggleFavorite(id); 
    } catch (error) {
      if (isMounted.current) {
        setHospitals(previousHospitals);
        setFilteredHospitals(previousHospitals);
        alert("로그인이 필요합니다.");
      }
    }
  };

  const handleHospitalClick = (h: any) => {
    if (!h.lat || !h.lng) return; 
    setSelectedId(h.id);
    setMapCenter({ lat: h.lat, lng: h.lng });
    if (map) map.setLevel(3); 
  };

  const handleOpenRoadview = (h: any) => {
    setRoadviewPos({ lat: h.lat, lng: h.lng });
    setIsRoadviewOpen(true);
  };

  const handleFilter = (cat: string) => {
    setFilterCategory(cat);
    setVisibleCount(6); 
    setSelectedId(null); 
  };

  return (
    <div className="w-full bg-[#fbfcfd] min-h-screen pb-24">
      {/* 1. 헤더 섹션 */}
      <div className="bg-white border-b border-slate-100 pt-20 pb-10">
        <div className="max-w-7xl mx-auto px-6">
          <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 sm:mb-16">
            <div className="space-y-5">
              <div className="inline-flex items-center gap-2 px-3 py-1 bg-green-50 text-green-700 rounded-full text-xs font-black tracking-tight">
                <span className="relative flex h-2 w-2">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                  <span className="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
                </span>
                VERIFIED SPECIALISTS
              </div>
              <h2 className="text-3xl lg:text-5xl font-extrabold text-slate-900 tracking-tight leading-[1.1]">
                <span className="text-transparent bg-clip-text bg-linear-to-r from-green-600 to-green-400">대전 전문의를 </span>찾아서
              </h2>
              <p className="text-slate-500 text-sm font-medium leading-relaxed max-w-2xl">보건복지부 인증 전문의가 상주하는 대전의 믿을 수 있는 병원 리스트입니다.</p>
            </div>

            <div className="relative w-full lg:w-96 mb-15">
              <Search className="absolute left-5 top-1/2 -translate-y-1/2 text-slate-400" size={20} />
              <input
                type="text"
                placeholder="병원명, 진료과목, 주소 검색..."
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                className="w-full pl-12 pr-12 py-4 bg-white border border-slate-200 rounded-3xl text-sm font-bold shadow-sm focus:outline-none focus:ring-4 focus:ring-green-500/10 focus:border-green-500 transition-all"
              />
              {keyword && (
                <button onClick={() => setKeyword("")} className="absolute right-4 top-1/2 -translate-y-1/2 p-1 hover:bg-slate-100 rounded-full text-slate-400 hover:text-green-600 transition-colors">
                  <X size={16} />
                </button>
              )}
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            {categories.map((cat) => (
              <button
                key={cat}
                onClick={() => handleFilter(cat)}
                className={`px-6 py-2.5 rounded-full text-sm font-bold transition-all ${
                  filterCategory === cat ? "bg-green-600 text-white shadow-lg shadow-green-100" : "bg-white border border-slate-200 text-slate-600 hover:border-slate-900"
                }`}
              >
                {cat}
              </button>
            ))}
          </div>
        </div>
      </div>

      <div className="w-full lg:max-w-7xl mx-auto px-4 lg:px-5 mt-10">
        <div className="grid grid-cols-1 lg:grid-cols-5 gap-8 items-start relative">
          <div className="w-full lg:col-span-2 space-y-6 order-1">
            <div className="p-4"><div className="mb-6"><AiHospitalSearch onSelectHospital={handleHospitalClick} /></div></div>

            <div className="flex items-center justify-between px-2 mb-4">
              <div className="flex flex-col">
                <span className="text-[10px] font-black text-slate-300 uppercase tracking-widest">Medical List</span>
                <p className="text-sm font-bold text-slate-500">{filterCategory}</p>
              </div>
              <span className="text-xs font-bold text-slate-400 bg-slate-100 px-3 py-1 rounded-full">{dataLoading ? "..." : `${filteredHospitals.length}개`}</span>
            </div>

            {dataLoading ? (
              Array(4).fill(0).map((_, i) => <HospitalListSkeleton key={i} />)
            ) : filteredHospitals.length === 0 ? (
              <div className="py-20 flex flex-col items-center justify-center bg-white rounded-[2.5rem] border border-slate-100 border-dashed relative overflow-hidden">
                <p className="text-slate-800 font-bold text-lg mb-1">검색된 병원이 없습니다.</p>
              </div>
            ) : (
              <>
                {filteredHospitals.slice(0, visibleCount).map((h) => (
                  <div
                    key={h.id}
                    onClick={() => handleHospitalClick(h)}
                    className={`group bg-white rounded-[2.5rem] p-8 border transition-all cursor-pointer relative ${
                      selectedId === h.id ? "border-green-500 shadow-2xl shadow-green-500/10 -translate-y-1" : "border-slate-100 hover:border-green-200 shadow-sm"
                    }`}
                  >
                    <div className="flex justify-between items-start mb-4">
                      <div className="flex items-center gap-2">
                        <div className="inline-flex items-center gap-2 px-3 py-1 bg-green-500 text-white text-[10px] font-black rounded-md uppercase tracking-widest shadow-lg shadow-green-200">{h.treatCategory}</div>
                        <button onClick={(e) => toggleFavorite(e, h.id)} className={`w-9 h-9 rounded-full flex items-center justify-center transition-all ${h.isFavorite ? "bg-red-50 text-red-500" : "bg-slate-50 text-slate-300 hover:bg-slate-100"}`}>
                          <Heart size={18} className={h.isFavorite ? "fill-red-500" : ""} />
                        </button>
                      </div>
                      <div className={`w-11 h-11 rounded-2xl flex items-center justify-center transition-all ${selectedId === h.id ? "bg-green-600 text-white shadow-lg shadow-green-200" : "bg-slate-50 text-slate-300 group-hover:bg-slate-100"}`}>
                        <MapIcon size={20} />
                      </div>
                    </div>
                    <h4 className="text-2xl font-black text-slate-900 mb-4 tracking-tight line-clamp-1">{h.name}</h4>
                    <div className="flex flex-col gap-3 mb-8 text-slate-500 text-sm font-medium">
                      <div className="flex items-center gap-2.5">
                        <MapPin size={16} className="text-green-500 shrink-0" />
                        <span className="line-clamp-1">{h.address}</span>
                      </div>
                    </div>
                    <div className="pt-6 border-t border-dashed border-slate-100 flex flex-col gap-1 sm:gap-0 sm:flex-row sm:items-center justify-between">
                      <div className="flex items-center gap-2"><div className="w-2 h-2 bg-green-500 rounded-full animate-pulse" /><span className="text-[11px] font-black text-slate-400 uppercase">Clinic Open</span></div>
                      <button onClick={(e) => { e.stopPropagation(); router.push(`/hospital/${h.id}`); }} className="flex items-center justify-center sm:justify-start gap-2 px-7 py-3.5 bg-slate-900 text-white rounded-2xl text-[13px] font-bold hover:bg-green-600 transition-all shadow-xl shadow-slate-200">진료 정보 보기 <ArrowRight size={16} /></button>
                    </div>
                  </div>
                ))}
                {visibleCount < filteredHospitals.length && (
                  <button onClick={() => setVisibleCount((v) => v + 5)} className="w-full py-6 bg-white border-2 border-slate-100 rounded-[2.2rem] text-slate-400 font-black text-sm hover:text-green-600 transition-all flex items-center justify-center gap-2"><Plus size={20} /> 결과 더 보기</button>
                )}
              </>
            )}
          </div>

          {/* 3. 우측 지도 섹션 */}
          <div id="hospital-map-section" className="w-full h-[500px] lg:h-[calc(100vh-140px)] lg:col-span-3 lg:sticky lg:top-[100px] lg:self-start mt-8 lg:mt-0 order-2">
            <div className="w-full h-full rounded-[3.5rem] overflow-hidden border-12px border-white shadow-2xl relative bg-slate-50">
              {isMapReady ? (
                <Map center={mapCenter} isPanto={true} style={{ width: "100%", height: "100%" }} level={7} onCreate={setMap}>
                  <MarkerClusterer averageCenter={true} minLevel={5} key={filterCategory}>
                    {filteredHospitals.map((h) => h.lat && (
                      <MapMarker key={`marker-${h.id}`} position={{ lat: h.lat, lng: h.lng }} onClick={() => setSelectedId(h.id)} image={{ src: makerImg.src, size: { width: 32, height: 32 }, options: { offset: { x: 16, y: 32 } } }}>
                        {selectedId === h.id && (
                          <div className="p-0 min-w-64 overflow-hidden rounded-2xl shadow-2xl bg-white border-none">
                            <div className="bg-slate-900 p-5 text-white">
                              <div className="flex justify-between items-start">
                                <p className="text-[10px] font-bold text-green-400 tracking-widest uppercase mb-1">{h.treatCategory}</p>
                                <button onClick={(e) => toggleFavorite(e, h.id)}><Heart size={16} className={h.isFavorite ? "fill-red-500 text-red-500" : "text-slate-400"} /></button>
                              </div>
                              <h4 className="font-bold text-base truncate">{h.name}</h4>
                            </div>
                            <div className="p-4 space-y-2">
                              <button onClick={() => handleOpenRoadview(h)} className="w-full flex items-center justify-center gap-2 py-3 bg-green-600 text-white rounded-xl text-xs font-black hover:bg-green-700 transition-all shadow-lg"><Camera size={14} /> 로드뷰 보기</button>
                              <button onClick={() => router.push(`/hospital/${h.id}`)} className="w-full py-3 bg-slate-50 text-slate-700 rounded-xl text-xs font-black hover:bg-slate-100 transition-all">상세정보</button>
                            </div>
                          </div>
                        )}
                      </MapMarker>
                    ))}
                  </MarkerClusterer>
                  {isRoadviewOpen && (
                    <div className="absolute inset-0 z-50 bg-slate-900/60 backdrop-blur-sm p-4 flex items-center justify-center">
                      <div className="w-full h-full bg-white rounded-3xl overflow-hidden relative shadow-2xl">
                        <div className="absolute top-6 right-6 z-60"><button onClick={() => setIsRoadviewOpen(false)} className="p-3 bg-slate-900 text-white rounded-full shadow-xl hover:scale-110 transition-transform active:scale-95"><X size={20} /></button></div>
                        <Roadview position={{ ...roadviewPos, radius: 50 }} className="w-full h-full" />
                      </div>
                    </div>
                  )}
                </Map>
              ) : (
                <div className="w-full h-full flex flex-col items-center justify-center gap-4">
                  <Loader2 className="animate-spin text-green-500" size={32} /><p className="text-slate-400 font-bold text-sm">지도를 불러오는 중입니다...</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}