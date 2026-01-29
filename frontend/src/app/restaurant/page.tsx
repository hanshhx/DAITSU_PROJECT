"use client"; // 이 파일이 브라우저(클라이언트 사이드)에서 실행되는 컴포넌트임을 Next.js에게 알립니다.

// --- [Imports] React 및 외부 라이브러리, 내부 모듈들을 불러옵니다 ---
import React, {
  useEffect, // 컴포넌트 마운트/업데이트 시 사이드 이펙트(데이터 가져오기 등)를 처리하는 훅
  useState, // 컴포넌트 내부의 상태 값을 관리하는 훅
  useRef, // DOM 요소나 특정 값을 렌더링 없이 유지하기 위해 사용하는 훅 (지도 객체 참조용)
  useMemo, // 연산 비용이 높은 작업의 결과를 캐싱하여 성능을 최적화하는 훅 (필터링 로직용)
  useCallback, // 함수를 메모이제이션하여 불필요한 재생성을 방지하는 훅 (핸들러 최적화용)
  Suspense, // 비동기 컴포넌트 로딩 중 대체 UI(로딩 스피너)를 보여주기 위한 React 내장 컴포넌트
} from "react";
import Link from "next/link"; // 페이지 이동을 위한 Next.js 링크 컴포넌트 (SPA 방식 이동)
import { restaurantService, userService } from "@/api/services"; // 백엔드 통신을 위한 API 서비스 함수들
import { RestaurantData } from "@/types/restaurant"; // 맛집 데이터의 타입 정의 (TypeScript 인터페이스)
import { useRouter, useSearchParams, usePathname } from "next/navigation"; // URL 경로 및 쿼리 파라미터를 제어하는 훅들
// UI에 사용될 아이콘들을 lucide-react 라이브러리에서 가져옵니다.
import {
  MapPin, // 지도 핀 아이콘
  Heart, // 하트(찜하기) 아이콘
  Search, // 돋보기 아이콘
  X, // 닫기(취소) 아이콘
  Clock, // 시계 아이콘
  Check, // 체크 아이콘
  Map as MapIcon, // 지도 모양 아이콘
  List as ListIcon, // 리스트 모양 아이콘
  Loader2, // 로딩 스피너 아이콘
  ChevronLeft, // 왼쪽 화살표
  ChevronRight, // 오른쪽 화살표
  AlertCircle, // 경고 느낌표 아이콘
  Eye, // 눈(보기) 아이콘
  Undo2, // 되돌리기 아이콘
} from "lucide-react";
import Pagination from "@/components/common/Pagination"; // 페이지네이션 처리를 위한 공통 컴포넌트
// 카카오맵 관련 기능을 사용하기 위해 react-kakao-maps-sdk에서 컴포넌트들을 가져옵니다.
import {
  Map as KakaoMap, // 지도 본체 컴포넌트
  MapMarker, // 지도 위 마커 컴포넌트
  MarkerClusterer, // 마커가 겹칠 때 그룹화해주는 컴포넌트
  CustomOverlayMap, // 마커 위에 커스텀 HTML을 띄우기 위한 오버레이 컴포넌트
  Roadview, // 로드뷰를 보여주는 컴포넌트
} from "react-kakao-maps-sdk";

import makerImg from "../../../public/images/mapMaker.png"; // 지도에 표시할 커스텀 마커 이미지 경로

// --- [타입 정의] 기본 맛집 데이터 인터페이스를 확장하여 UI 상태를 포함합니다 ---
interface ExtendedRestaurantData extends RestaurantData {
  restOpenTime?: string; // 영업시간 정보 (문자열 형태)
  businessStatus?: "OPEN" | "BREAK" | "CLOSED"; // 현재 영업 상태 (영업중, 휴게시간, 종료)
  todayHours?: string; // 오늘 날짜 기준 영업 시간 텍스트
  lat?: number; // 위도 (Geocoding 결과)
  lng?: number; // 경도 (Geocoding 결과)
}

// ==================================================================
// [Component 1] RestaurantListItem
// 지도 뷰의 사이드바에 표시되는 개별 맛집 리스트 아이템 컴포넌트입니다.
// React.memo를 사용하여 props가 변경되지 않으면 리렌더링되지 않도록 최적화했습니다.
// ==================================================================
const RestaurantListItem = React.memo(
  ({
    item, // 렌더링할 맛집 데이터 객체
    activeId, // 현재 선택(활성화)된 맛집의 ID
    onClick, // 아이템 클릭 시 실행될 부모 컴포넌트의 함수 (ID를 전달)
    onFavorite, // 찜하기 버튼 클릭 시 실행될 부모 컴포넌트의 함수
  }: {
    item: ExtendedRestaurantData; // 타입 정의
    activeId: number | null;
    onClick: (id: number) => void;
    onFavorite: (e: React.MouseEvent, id: number) => void;
  }) => {
    return (
      <div
        onClick={() => onClick(item.id)} // 클릭 이벤트 발생 시 해당 ID를 부모에게 전달
        className={`flex gap-4 p-4 border-b border-slate-100 cursor-pointer transition-colors bg-white hover:bg-slate-50 ${
          activeId === item.id
            ? "bg-green-50 border-green-200 ring-1 ring-inset ring-green-200" // 활성화 상태일 때 초록색 강조 스타일 적용
            : ""
        }`}
      >
        {/* 썸네일 이미지 영역 */}
        <div className="relative w-20 h-20 rounded-lg overflow-hidden shrink-0 border border-slate-100 bg-slate-100">
          <img
            src={`/images/restaurantImages/${item.imagePath}`} // 이미지 경로 설정
            alt={item.name} // 접근성을 위한 대체 텍스트
            className="w-full h-full object-cover" // 이미지가 영역을 꽉 채우도록 설정
            loading="lazy" // 성능 최적화를 위해 이미지를 지연 로딩함
          />
        </div>

        {/* 텍스트 정보 영역 (이름, 주소, 태그) */}
        <div className="flex-1 min-w-0 flex flex-col justify-center">
          <div className="flex justify-between items-start">
            <h4
              className={`font-bold text-sm truncate ${
                activeId === item.id ? "text-green-700" : "text-slate-900" // 활성화 여부에 따른 텍스트 색상 변경
              }`}
            >
              {item.name} {/* 가게 이름 표시 */}
            </h4>
          </div>
          <p className="text-xs text-slate-500 mt-0.5 truncate">
            {item.address} {/* 주소 표시 (길면 말줄임표) */}
          </p>

          {/* 태그 영역 (카테고리 및 영업 상태 뱃지) */}
          <div className="flex flex-wrap items-center gap-2 mt-2">
            <span className="text-[10px] font-bold text-slate-500 border border-slate-200 px-1.5 py-0.5 rounded bg-white">
              {item.restCategory} {/* 음식 카테고리 표시 */}
            </span>
            {/* 영업 상태에 따라 다른 색상과 텍스트의 뱃지를 조건부 렌더링 */}
            {item.businessStatus === "OPEN" && (
              <span className="text-[10px] font-bold text-green-600 flex items-center gap-0.5">
                <span className="w-1.5 h-1.5 rounded-full bg-green-500"></span>{" "}
                영업중
              </span>
            )}
            {item.businessStatus === "BREAK" && (
              <span className="text-[10px] font-bold text-orange-500">
                브레이크타임
              </span>
            )}
            {item.businessStatus === "CLOSED" && (
              <span className="text-[10px] font-bold text-slate-400">
                영업종료
              </span>
            )}
          </div>
        </div>

        {/* 찜하기(하트) 버튼 - 우측 상단 절대 위치 */}
        <button
          onClick={(e) => onFavorite(e, item.id)} // 클릭 시 onFavorite 함수 실행
          className="absolute top-2 right-2 p-2 text-slate-300 hover:text-orange-500 transition-colors"
        >
          <Heart
            size={16}
            className={item.isFavorite ? "fill-orange-500 text-orange-500" : ""} // 찜 상태면 색칠된 하트 표시
          />
        </button>
      </div>
    );
  },
);
RestaurantListItem.displayName = "RestaurantListItem"; // React DevTools에서 컴포넌트 이름을 식별하기 위해 설정

// ==================================================================
// [Component 2] KakaoMapContainer
// 실제 카카오맵을 렌더링하고 마커 이벤트를 처리하는 컨테이너 컴포넌트입니다.
// 지도 로직이 복잡하므로 별도 컴포넌트로 분리하고 Memoization을 적용했습니다.
// ==================================================================
const KakaoMapContainer = React.memo(
  ({
    data, // 지도에 표시할 전체 맛집 데이터 배열
    activeId, // 현재 선택된 맛집 ID
    isSidebarOpen, // 사이드바 열림/닫힘 상태 (지도 리레이아웃 트리거용)
    onMarkerClick, // 마커 클릭 시 실행할 함수
    onMapClick, // 지도 빈 공간 클릭 시 실행할 함수
  }: {
    data: ExtendedRestaurantData[];
    activeId: number | null;
    isSidebarOpen: boolean;
    onMarkerClick: (item: ExtendedRestaurantData) => void;
    onMapClick: () => void;
  }) => {
    const mapRef = useRef<kakao.maps.Map | null>(null); // 카카오맵 인스턴스를 저장할 Ref

    // 중복 로더 에러 방지를 위해 layout.tsx의 전역 스크립트를 감시하도록 변경
    const [isReady, setIsReady] = useState(false);
    const isMounted = useRef(true);

    // 로드뷰 모드 상태 관리
    const [isRoadviewMode, setIsRoadviewMode] = useState(false); 
    const [roadviewPosition, setRoadviewPosition] = useState<{
      lat: number;
      lng: number;
      radius: number;
    }>({
      lat: 0,
      lng: 0,
      radius: 50, 
    });

    // 전역 kakao 객체가 준비되었는지 반복 체크 (Loader 중복 방지)
    useEffect(() => {
      isMounted.current = true;
      const checkKakao = () => {
        if (window.kakao && window.kakao.maps) {
          window.kakao.maps.load(() => {
            if (isMounted.current) setIsReady(true);
          });
        }
      };
      checkKakao();
      const timer = setInterval(checkKakao, 1000);
      return () => {
        isMounted.current = false;
        clearInterval(timer);
      };
    }, []);

    // 사이드바가 열리거나 닫힐 때 지도의 크기가 변하므로 relayout을 호출하여 깨짐을 방지합니다.
    useEffect(() => {
      if (mapRef.current) {
        setTimeout(() => {
          mapRef.current?.relayout(); 
        }, 300); 
      }
    }, [isSidebarOpen]);

    // 활성화된 아이템(activeId)이 변경되면 해당 위치로 지도를 이동시킵니다.
    useEffect(() => {
      if (!mapRef.current || !activeId || isRoadviewMode) return; 

      const target = data.find((d) => d.id === activeId); 

      // 좌표 정보가 유효한 경우에만 이동
      if (
        target &&
        typeof target.lat === "number" &&
        typeof target.lng === "number"
      ) {
        const moveLatLon = new kakao.maps.LatLng(target.lat, target.lng);
        const currentLevel = mapRef.current.getLevel();

        if (currentLevel > 4) {
          mapRef.current.setLevel(3, { animate: true }); 
          setTimeout(() => {
            mapRef.current?.panTo(moveLatLon);
          }, 150);
        } else {
          mapRef.current.panTo(moveLatLon);
        }
      }
    }, [activeId, data, isRoadviewMode]);

    // 로드뷰 버튼 클릭 핸들러 (좌표를 받아 로드뷰 모드를 켬)
    const handleOpenRoadview = useCallback((lat: number, lng: number) => {
      setRoadviewPosition({ lat, lng, radius: 50 });
      setIsRoadviewMode(true);
    }, []);

    // 현재 활성화된 아이템 데이터를 계산 (오버레이 표시용)
    const activeItem = useMemo(
      () => data.find((d) => d.id === activeId),
      [data, activeId],
    );

    // 지도가 준비되지 않았을 때 로딩 표시
    if (!isReady) {
      return (
        <div className="flex flex-col items-center justify-center h-full bg-slate-50">
          <Loader2 className="animate-spin text-green-500 mb-2" size={32} />
          <p className="text-sm font-bold text-slate-400">지도를 준비 중입니다...</p>
        </div>
      );
    }

    // 로드뷰 모드일 경우 로드뷰 컴포넌트 렌더링
    if (isRoadviewMode) {
      return (
        <div className="relative w-full h-full">
          <button
            onClick={() => setIsRoadviewMode(false)}
            className="absolute top-4 left-4 z-50 bg-white px-4 py-2 rounded-lg shadow-lg border border-slate-200 text-slate-700 font-bold flex items-center gap-2 hover:bg-slate-50 transition-transform hover:scale-105"
          >
            <Undo2 size={18} /> 지도 보기
          </button>
          <Roadview position={roadviewPosition} className="w-full h-full" />
        </div>
      );
    }

    // 기본 지도 렌더링
    return (
      <div className="w-full h-full [&_img]:max-w-none [&_img]:h-auto [&_img]:border-none">
        <KakaoMap
          center={{ lat: 36.3504, lng: 127.3845 }} // 초기 중심 좌표 (대전 시청)
          className="w-full h-full"
          level={7} 
          onCreate={(map) => (mapRef.current = map)} 
          onClick={onMapClick} 
        >
          {/* 마커 클러스터러: 마커가 겹칠 때 그룹화하여 숫자로 표시 */}
          <MarkerClusterer averageCenter={true} minLevel={6}>
            {data.map(
              (item) =>
                // 좌표가 있는 데이터만 마커 생성
                typeof item.lat === "number" &&
                typeof item.lng === "number" && (
                  <MapMarker
                    key={`marker-${item.id}`} // 유니크한 키 부여
                    position={{ lat: item.lat, lng: item.lng }}
                    onClick={() => onMarkerClick(item)} // 마커 클릭 시 활성화
                    image={{
                      src:
                        activeId === item.id
                          ? "https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png" 
                          : makerImg.src, 
                      size:
                        activeId === item.id
                          ? { width: 29, height: 42 }
                          : { width: 34, height: 35 },
                      options: {
                        offset:
                          activeId === item.id
                            ? { x: 14.5, y: 42 }
                            : { x: 12, y: 35 },
                      },
                    }}
                    zIndex={activeId === item.id ? 9999 : 1} 
                    clickable={true}
                  />
                ),
            )}
          </MarkerClusterer>

          {activeItem && activeItem.lat && activeItem.lng && (
            <CustomOverlayMap
              position={{ lat: activeItem.lat, lng: activeItem.lng }}
              yAnchor={1.4} 
              zIndex={10000}
              clickable={true}
            >
              <div
                className="bg-white p-3 rounded-xl shadow-2xl border border-slate-100 w-56 animate-in zoom-in duration-200 relative pointer-events-auto cursor-default"
                onClick={(e) => e.stopPropagation()} 
              >
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onMapClick(); 
                  }}
                  className="absolute top-2 right-2 text-slate-300 hover:text-red-500 bg-white rounded-full p-0.5 transition-colors z-10"
                >
                  <X size={16} />
                </button>

                <div className="mb-2 pr-5">
                  <h5 className="font-black text-sm text-slate-900 truncate">
                    {activeItem.name}
                  </h5>
                  <p className="text-[10px] text-slate-500 truncate mt-0.5">
                    {activeItem.address}
                  </p>
                </div>

                <div className="flex gap-2 mt-3">
                  <Link
                    href={`/restaurant/${activeItem.id}`} 
                    className="flex-1 py-2 bg-slate-900 hover:bg-slate-800 text-white text-xs font-bold text-center rounded-lg transition-colors"
                  >
                    상세정보
                  </Link>
                  <button
                    onClick={() =>
                      handleOpenRoadview(activeItem.lat!, activeItem.lng!)
                    }
                    className="px-3 py-2 bg-blue-50 hover:bg-blue-100 text-blue-600 border border-blue-100 rounded-lg transition-colors flex items-center justify-center"
                    title="로드뷰 보기"
                  >
                    <Eye size={16} />
                  </button>
                </div>
              </div>
            </CustomOverlayMap>
          )}
        </KakaoMap>
      </div>
    );
  },
);
KakaoMapContainer.displayName = "KakaoMapContainer";

// ==================================================================
// [Component 3] RestaurantPageContent
// 메인 페이지의 로직과 UI를 담당하는 컴포넌트입니다.
// ==================================================================
function RestaurantPageContent() {
  const router = useRouter(); // 페이지 라우팅용 훅
  const pathname = usePathname(); // 현재 경로 확인용 훅
  const searchParams = useSearchParams(); // URL 쿼리 파라미터 읽기용 훅

  // --- [State] 데이터 및 UI 상태 관리 ---
  const [restaurants, setRestaurants] = useState<ExtendedRestaurantData[]>([]); // 맛집 데이터 목록

  const [loading, setLoading] = useState(true); // 데이터 로딩 상태
  const [activeId, setActiveId] = useState<number | null>(null); // 현재 선택된 맛집 ID
  const [isSidebarOpen, setIsSidebarOpen] = useState(true); // 지도 뷰에서 사이드바 열림 상태
  const itemsPerPage = 8; // 페이지당 아이템 수

  // 컴포넌트 마운트 상태 관리 (Application Error 방지)
  const isMounted = useRef(true);
  useEffect(() => {
    isMounted.current = true;
    return () => { isMounted.current = false; };
  }, []);

  // 지도 중심 좌표 상태
  const [mapCenter, setMapCenter] = useState({
    lat: 36.3504,
    lng: 127.3845,
  });

  const [isMapReadyForGeocode, setIsMapReadyForGeocode] = useState(false);

  // URL 쿼리 파라미터에서 필터 상태 가져오기
  const currentCategory = searchParams.get("category") || "전체";
  const currentKeyword = searchParams.get("keyword") || "";
  const showOpenOnly = searchParams.get("open") === "true"; 
  const isMapView = searchParams.get("view") === "map"; 
  const currentPage = Number(searchParams.get("page")) || 1; 

  const [tempKeyword, setTempKeyword] = useState(currentKeyword); // 검색어 입력용 임시 상태

  // 전역 카카오 객체 준비 확인 로직 (지오코딩용)
  useEffect(() => {
    const checkMap = () => {
      if (window.kakao && window.kakao.maps?.services) {
        if (isMounted.current) setIsMapReadyForGeocode(true);
      }
    };
    checkMap();
    const t = setInterval(checkMap, 1000);
    return () => clearInterval(t);
  }, []);

  // --- [Helper Functions] 시간 계산 및 영업 상태 로직 ---
  const parseTime = useCallback((str: string) => {
    const [h, m] = str.split(":").map(Number);
    return h * 60 + m;
  }, []);

  const getBusinessStatus = useCallback(
    (
      timeString: string | undefined,
    ): { status: "OPEN" | "BREAK" | "CLOSED"; todayStr: string } => {
      if (!timeString) return { status: "CLOSED", todayStr: "정보 없음" };

      const now = new Date();
      const dayIndex = now.getDay(); 
      const currentMinutes = now.getHours() * 60 + now.getMinutes();
      const daysKor = ["일", "월", "화", "수", "목", "금", "토"]; 
      const todayShort = daysKor[dayIndex]; 

      if (
        timeString.includes(`${todayShort}요일 휴무`) ||
        timeString.includes(`${todayShort}요일휴무`)
      ) {
        return { status: "CLOSED", todayStr: "금일 휴무" };
      }

      const timeMatch = timeString.match(
        /(\d{1,2}:\d{2})\s*[~\-]\s*(\d{1,2}:\d{2})/,
      );

      if (!timeMatch) return { status: "CLOSED", todayStr: timeString };

      const [_, openStr, closeStr] = timeMatch;
      const openMin = parseTime(openStr);
      let closeMin = parseTime(closeStr);

      if (closeMin < openMin) closeMin += 24 * 60;

      let adjustedCurrent = currentMinutes;
      if (currentMinutes < openMin && closeMin >= 24 * 60) {
        if (currentMinutes < closeMin - 24 * 60) {
          adjustedCurrent += 24 * 60;
        }
      }

      const breakMatch = timeString.match(
        /(\d{1,2}:\d{2})\s*[~\-]\s*(\d{1,2}:\d{2}).*(브레이크|break)/i,
      );
      if (breakMatch) {
        const [__, bStart, bEnd] = breakMatch;
        const bStartMin = parseTime(bStart);
        const bEndMin = parseTime(bEnd);
        if (adjustedCurrent >= bStartMin && adjustedCurrent < bEndMin) {
          return { status: "BREAK", todayStr: `${openStr} ~ ${closeStr}` };
        }
      }

      if (adjustedCurrent >= openMin && adjustedCurrent < closeMin) {
        return { status: "OPEN", todayStr: `${openStr} ~ ${closeStr}` };
      }
      return { status: "CLOSED", todayStr: `${openStr} ~ ${closeStr}` };
    },
    [parseTime],
  );

  // --- [Effect] 데이터 가져오기 ---
  useEffect(() => {
    const fetchRestaurants = async () => {
      try {
        setLoading(true);
        const [restaurantsRes, favoritesRes] = await Promise.allSettled([
          restaurantService.getRestaurants(),
          userService.getFavorites(),
        ]);

        let allRestaurants: any[] = [];
        const myFavoriteIds = new Set<number>(); 

        if (restaurantsRes.status === "fulfilled")
          allRestaurants = restaurantsRes.value.data;
        if (favoritesRes.status === "fulfilled") {
          const favoriteList = favoritesRes.value.data;
          if (Array.isArray(favoriteList))
            favoriteList.forEach((item: any) => myFavoriteIds.add(item.id));
        }

        const mergedList = allRestaurants.map((item) => {
          const { status, todayStr } = getBusinessStatus(
            item.restOpenTime || item.openTime,
          );
          return {
            ...item,
            isFavorite: myFavoriteIds.has(item.id), 
            businessStatus: status, 
            todayHours: todayStr, 
          } as ExtendedRestaurantData;
        });

        if (isMounted.current) setRestaurants(mergedList);
      } catch (error) {
        console.error("Error fetching:", error);
      } finally {
        if (isMounted.current) setLoading(false);
      }
    };
    fetchRestaurants();
  }, [getBusinessStatus]);

  // --- [Memo] 필터링 로직 ---
  const filteredList = useMemo(() => {
    let result = restaurants;

    if (currentCategory !== "전체") {
      result = result.filter((item) => item.restCategory === currentCategory);
    }

    const trimmedKeyword = currentKeyword.trim().toLowerCase();
    if (trimmedKeyword !== "") {
      const searchTerms = trimmedKeyword.split(/\s+/); 
      result = result.filter((item) => {
        const name = (item.name || "").toLowerCase();
        const menu = (item.menu || []).join(" ").toLowerCase();
        const category = (item.restCategory || "").toLowerCase();
        const address = (item.address || "").toLowerCase();
        return searchTerms.every(
          (term) =>
            name.includes(term) ||
            menu.includes(term) ||
            category.includes(term) ||
            address.includes(term),
        );
      });
    }

    if (showOpenOnly) {
      result = result.filter((item) => item.businessStatus === "OPEN");
    }

    return result;
  }, [restaurants, currentCategory, currentKeyword, showOpenOnly]);

  // --- [Effect] 주소 -> 좌표 변환 (Geocoding) ---
  // ✅ [수정] 병원 페이지처럼 실시간으로 마커가 지도에 채워지는 방식으로 변경
  useEffect(() => {
    if (!isMapReadyForGeocode || restaurants.length === 0) return;

    const itemsToGeocode = restaurants.filter(
      (item) => !item.lat && typeof item.address === "string" && item.address.trim() !== ""
    );
    
    if (itemsToGeocode.length === 0) return;

    const geocoder = new window.kakao.maps.services.Geocoder();

    const processGeocoding = async () => {
      // 💡 현재 렌더링 중인 상태를 계속 업데이트하기 위해 원본 배열 복사
      let currentRestaurants = [...restaurants];

      for (let i = 0; i < itemsToGeocode.length; i++) {
        if (!isMounted.current) break; // 페이지 이동 시 즉시 중단

        const item = itemsToGeocode[i];

        await new Promise<void>((resolve) => {
          geocoder.addressSearch(item.address!, (result, status) => {
            if (status === window.kakao.maps.services.Status.OK && isMounted.current) {
              const idx = currentRestaurants.findIndex(r => r.id === item.id);
              if (idx !== -1) {
                // 좌표 데이터가 들어온 항목만 개별 업데이트
                currentRestaurants[idx] = { 
                  ...currentRestaurants[idx], 
                  lat: Number(result[0].y), 
                  lng: Number(result[0].x) 
                };
              }
            }
            // 💡 429 에러(과부하) 방지를 위한 짧은 대기 (병원과 동일한 80ms)
            setTimeout(resolve, 80);
          });
        });

        // ✅ [핵심수정] 5개 단위로 UI를 즉시 갱신하여 사용자가 진행 상황을 볼 수 있게 함
        // (병원 페이지의 "천천히 나오는 방식" 구현)
        if (i % 5 === 0 || i === itemsToGeocode.length - 1) {
          if (isMounted.current) setRestaurants([...currentRestaurants]);
        }
      }
    };

    processGeocoding();
  }, [isMapReadyForGeocode, restaurants.length > 0]); // 데이터 로딩 완료 시점을 감지

  // --- [Handlers] 이벤트 핸들러 모음 ---

  const toggleFavorite = useCallback(
    async (e: React.MouseEvent, id: number) => {
      e.preventDefault(); 
      e.stopPropagation(); 
      try {
        await restaurantService.toggleFavorite(id); 
        setRestaurants((prev) =>
          prev.map((item) =>
            item.id === id ? { ...item, isFavorite: !item.isFavorite } : item,
          ),
        );
      } catch (error) {
        alert("로그인이 필요합니다.");
      }
    },
    [],
  );

  const updateParams = (newParams: Record<string, string | null>) => {
    const params = new URLSearchParams(searchParams);
    Object.entries(newParams).forEach(([key, value]) => {
      if (value === null)
        params.delete(key); 
      else params.set(key, value); 
    });
    if (!newParams.page) params.set("page", "1"); 
    router.push(`${pathname}?${params.toString()}`); 
  };

  const handleFilter = (category: string) => updateParams({ category }); 
  const handleSearch = () => updateParams({ keyword: tempKeyword }); 
  const clearKeyword = () => {
    setTempKeyword("");
    updateParams({ keyword: null }); 
  };
  const toggleOpenOnly = () =>
    updateParams({ open: showOpenOnly ? null : "true" }); 
  const toggleView = () => updateParams({ view: isMapView ? null : "map" }); 
  const handlePageChange = (page: number) => {
    const params = new URLSearchParams(searchParams);
    params.set("page", String(page));
    router.push(`${pathname}?${params.toString()}`);
  };

  const handleMarkerClick = useCallback(
    (item: ExtendedRestaurantData) => setActiveId(item.id),
    [],
  );
  const handleMapClick = useCallback(() => setActiveId(null), []);

  const handleRestaurantClick = (id: number) => {
    setActiveId(id);
    const target = restaurants.find((item) => item.id === id);
    if (target && target.lat && target.lng) {
      setMapCenter({ lat: target.lat, lng: target.lng });
    }
    if (window.innerWidth < 1024) {
      const mapElement = document.getElementById("restaurant-map-section");
      if (mapElement) {
        mapElement.scrollIntoView({ behavior: "smooth" });
      }
    }
  };

  const totalPages = Math.ceil(filteredList.length / itemsPerPage);
  const currentItems = filteredList.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage,
  );

  // -----------------------------------------------------------
  // [Render] 화면 렌더링
  // -----------------------------------------------------------
  return (
    <div className="w-full bg-[#fcfcfc] min-h-screen pb-24 font-pretendard">
      {/* 헤더 섹션: 타이틀 및 검색창 */}
      <div className="bg-white border-b border-slate-100 pt-20 pb-10">
        <div className="max-w-7xl mx-auto px-6">
          <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 sm:mb-16">
            <div className="space-y-5">
              <div className="inline-flex items-center gap-2 px-3 py-1 bg-green-50 text-green-700 rounded-full text-xs font-black tracking-tight">
                <span className="relative flex h-2 w-2">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                  <span className="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
                </span>
                DAEJEON NOW
              </div>
              <h2 className="text-3xl lg:text-5xl font-extrabold text-slate-900 tracking-tight leading-[1.1]">
                <span className="text-transparent bg-clip-text bg-linear-to-r from-green-600 to-green-400">
                  대전의 맛
                </span>
                을 찾아서
              </h2>
              <p className="text-slate-500 text-sm font-medium leading-relaxed max-w-2xl">
                현지인이 추천하는 진짜 맛집 리스트를 카테고리별로 확인하세요.
              </p>
            </div>

            {!isMapView && (
              <div className="relative w-full lg:w-96 mb-15">
                <Search
                  className="absolute left-5 top-1/2 -translate-y-1/2 text-slate-400"
                  size={20}
                />
                <input
                  type="text"
                  placeholder="맛집 이름, 메뉴 검색..."
                  value={tempKeyword}
                  onChange={(e) => setTempKeyword(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                  className="w-full pl-12 pr-12 py-4 bg-white border border-slate-200 rounded-3xl text-sm font-bold shadow-sm focus:outline-none focus:ring-4 focus:ring-green-500/10 focus:border-green-500 transition-all"
                />
                {tempKeyword && (
                  <button
                    onClick={clearKeyword}
                    className="absolute right-4 top-1/2 -translate-y-1/2 p-1 hover:bg-slate-100 rounded-full text-slate-400 hover:text-green-600 transition-colors"
                  >
                    <X size={16} />
                  </button>
                )}
              </div>
            )}
          </div>

          <div className="flex flex-col sm:flex-row items-center justify-between mt-10 gap-4">
            <div className="flex flex-wrap items-center gap-2">
              {[
                "전체",
                "한식",
                "일식",
                "중식",
                "양식",
                "분식",
                "치킨",
                "카페·디저트",
              ].map((cat) => (
                <button
                  key={cat}
                  onClick={() => handleFilter(cat)}
                  className={`px-5 py-2.5 rounded-full text-sm font-bold transition-all ${
                    currentCategory === cat
                      ? "bg-green-600 text-white shadow-lg shadow-green-100"
                      : "bg-white border border-slate-200 text-slate-600 hover:border-slate-900"
                  }`}
                >
                  {cat}
                </button>
              ))}
            </div>

            <div className="flex flex-wrap items-center gap-3 w-full sm:justify-end">
              <button
                onClick={toggleOpenOnly}
                className={`flex items-center gap-2 px-5 py-2.5 rounded-full text-sm font-bold transition-all border shrink-0 ${
                  showOpenOnly
                    ? "bg-green-50 border-green-200 text-green-700 ring-2 ring-green-500/20"
                    : "bg-white border-slate-200 text-slate-500 hover:bg-slate-50"
                }`}
              >
                <div
                  className={`w-4 h-4 rounded-full border flex items-center justify-center transition-colors ${
                    showOpenOnly
                      ? "bg-green-500 border-green-500"
                      : "border-slate-300 bg-white"
                  }`}
                >
                  {showOpenOnly && <Check size={10} className="text-white" />}
                </div>
                <Clock size={16} />
                <span>영업 중만 보기</span>
              </button>

              <button
                onClick={toggleView}
                className={`flex items-center gap-2 px-5 py-2.5 rounded-full text-sm font-bold transition-all border shrink-0 ${
                  isMapView
                    ? "bg-slate-900 text-white border-slate-900"
                    : "bg-white border-slate-200 text-slate-900 hover:bg-slate-50"
                }`}
              >
                {isMapView ? (
                  <>
                    <ListIcon size={16} />
                    <span>리스트로 보기</span>
                  </>
                ) : (
                  <>
                    <MapIcon size={16} />
                    <span>지도로 보기</span>
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      </div>

      <div className="w-full lg:max-w-7xl mx-auto px-4 lg:px-5 mt-10">
        {isMapView ? (
          <div className="flex flex-col lg:flex-row gap-4 lg:gap-0 h-auto lg:h-[750px] w-full bg-transparent lg:bg-white lg:rounded-2xl lg:overflow-hidden lg:shadow-2xl lg:border lg:border-slate-200 relative">
            <div
              className={`flex flex-col transition-all duration-300 ease-in-out relative z-10 
                bg-white rounded-2xl shadow-sm border border-slate-200 lg:shadow-none lg:rounded-none lg:border-0 lg:border-r lg:border-slate-100
                ${
                  isSidebarOpen
                    ? "h-[400px] lg:h-full w-full lg:w-[400px] lg:min-w-[320px]"
                    : "h-0 lg:h-full w-full lg:w-0 lg:min-w-0 overflow-hidden"
                }`}
            >
              <div className="p-4 pb-2 bg-white sticky top-0 z-20">
                <div className="relative">
                  <Search
                    className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                    size={18}
                  />
                  <input
                    type="text"
                    placeholder="지도 내 검색"
                    value={tempKeyword}
                    onChange={(e) => setTempKeyword(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                    className="w-full pl-10 pr-10 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm font-bold focus:outline-none focus:ring-2 focus:ring-green-500 focus:bg-white transition-all"
                  />
                </div>
              </div>

              <div className="px-4 py-2 border-b border-slate-100 bg-white flex items-center justify-between shrink-0">
                <span className="text-sm font-bold text-slate-600">
                  검색 결과{" "}
                  <span className="text-green-600">{filteredList.length}</span>
                  개
                </span>
                <button
                  onClick={() => setIsSidebarOpen(false)}
                  className="p-1.5 hover:bg-slate-100 rounded-md text-slate-400"
                >
                  <ChevronRight size={16} className="lg:rotate-180 rotate-90" />
                </button>
              </div>

              <div className="flex-1 overflow-y-auto custom-scrollbar bg-slate-50/50">
                {filteredList.map((item) => (
                  <RestaurantListItem
                    key={item.id}
                    item={item}
                    activeId={activeId}
                    onClick={handleRestaurantClick}
                    onFavorite={toggleFavorite}
                  />
                ))}
              </div>
            </div>

            <div
              id="restaurant-map-section"
              className="relative bg-slate-100 overflow-hidden 
                w-full h-[500px] min-h-[500px] lg:h-full lg:flex-1
                rounded-2xl shadow-sm border border-slate-200 lg:rounded-none lg:shadow-none lg:border-0"
            >
              {!isSidebarOpen && (
                <button
                  onClick={() => setIsSidebarOpen(true)}
                  className="absolute top-4 left-4 z-20 bg-white p-2.5 rounded-lg shadow-md border border-slate-200 text-slate-600 hover:text-green-600 transition-transform hover:scale-105"
                >
                  <ChevronRight size={20} className="-rotate-90 lg:rotate-0" />
                </button>
              )}

              <KakaoMapContainer
                data={filteredList}
                activeId={activeId}
                isSidebarOpen={isSidebarOpen}
                onMarkerClick={handleMarkerClick}
                onMapClick={handleMapClick}
              />
            </div>
          </div>
        ) : filteredList.length === 0 ? (
          <div className="py-32 flex flex-col items-center justify-center bg-white rounded-[2.5rem] border border-dashed border-gray-200 text-center">
            <div className="text-4xl mb-4">😢</div>
            <h3 className="text-xl font-black text-slate-900 mb-2">
              검색된 맛집이 없습니다.
            </h3>
            <p className="text-slate-500 text-sm">
              다른 키워드로 검색해보세요.
            </p>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
              {currentItems.map((item) => (
                <div key={item.id} className="relative group">
                  <button
                    onClick={(e) => toggleFavorite(e, item.id)}
                    className="absolute top-4 right-4 z-20 p-2.5 rounded-full bg-white/80 backdrop-blur-md shadow-sm transition-all hover:scale-110 active:scale-90 border border-slate-100"
                  >
                    <Heart
                      size={18}
                      className={`${
                        item.isFavorite
                          ? "fill-orange-500 text-orange-500"
                          : "text-slate-400"
                      }`}
                    />
                  </button>
                  <Link
                    href={`/restaurant/${item.id}`}
                    className="block h-full"
                  >
                    <div className="bg-white rounded-[2.5rem] overflow-hidden shadow-sm hover:shadow-xl transition-all duration-500 border border-slate-100 h-full flex flex-col">
                      <div className="relative aspect-video overflow-hidden">
                        <img
                          src={`/images/restaurantImages/${item.imagePath}`}
                          className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
                          alt={item.name}
                          loading="lazy"
                        />
                        <div className="absolute top-4 left-4 bg-white/95 backdrop-blur-sm px-3 py-1 rounded-full text-[10px] font-black text-green-600 shadow-sm">
                          {item.restCategory}
                        </div>
                        {item.businessStatus &&
                          item.businessStatus !== "CLOSED" && (
                            <div
                              className={`absolute bottom-4 left-4 px-3 py-1 rounded-full text-[10px] font-bold shadow-md flex items-center gap-1 text-white ${
                                item.businessStatus === "OPEN"
                                  ? "bg-green-500"
                                  : "bg-orange-500"
                              }`}
                            >
                              <Clock size={10} />
                              {item.businessStatus === "OPEN"
                                ? "영업중"
                                : "브레이크타임"}
                            </div>
                          )}
                        {item.businessStatus === "CLOSED" && (
                          <div className="absolute inset-0 bg-black/40 flex items-center justify-center backdrop-blur-[2px]">
                            <span className="text-white font-black border-2 border-white px-4 py-2 rounded-xl">
                              영업종료
                            </span>
                          </div>
                        )}
                      </div>

                      <div className="p-6 flex flex-col grow">
                        <h3 className="text-lg font-black text-slate-900 mb-2 group-hover:text-green-600 transition-colors">
                          {item.name}
                        </h3>
                        <div className="flex items-center gap-1 text-slate-400 text-[11px] mb-4 font-medium">
                          <MapPin size={12} className="text-slate-300" />
                          <span className="line-clamp-1">{item.address}</span>
                        </div>
                        <div className="mt-auto pt-4 border-t border-slate-50 flex flex-col gap-1">
                          <span className="text-[10px] text-slate-400 uppercase tracking-widest font-bold">
                            Best Menu
                          </span>
                          <span className="text-orange-600 font-bold text-sm truncate">
                            {item.bestMenu || "추천메뉴"}
                          </span>
                        </div>
                      </div>
                    </div>
                  </Link>
                </div>
              ))}
            </div>

            <div className="mt-12">
              <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                onPageChange={handlePageChange}
                themeColor="black"
              />
            </div>
          </>
        )}
      </div>
    </div>
  );
}

// --- [최상위 페이지 컴포넌트] ---
export default function RestaurantListPage() {
  return (
    <Suspense
      fallback={
        <div className="h-screen flex justify-center items-center">
          <Loader2 className="animate-spin text-green-500" />
        </div>
      }
    >
      <RestaurantPageContent />
    </Suspense>
  );
}