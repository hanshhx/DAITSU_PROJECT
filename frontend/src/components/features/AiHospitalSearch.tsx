// 1. "use client": Next.js에서 이 파일이 브라우저(클라이언트)에서 실행됨을 선언합니다.
"use client";

// --- [라이브러리 및 훅 임포트] ---
import { useState, useEffect } from "react"; 
import { useRouter } from "next/navigation"; 
import {
  Sparkles,
  Search,
  Activity,
  X,
  MapPin,
  ChevronRight,
  AlertCircle,
  AlertTriangle,
  Siren,
  Clock,
  CheckCircle2,
  ExternalLink,
} from "lucide-react"; 
import { hospitalService } from "@/api/services"; 

// --- [타입 정의] ---
interface AiHospitalSearchProps {
  onSelectHospital?: (hospital: any) => void;
}

type UrgencyLevel = "EMERGENCY" | "URGENT" | "NORMAL";

// --- [분석 규칙 정의] ---
const ANALYSIS_RULES = [
  {
    keywords: ["숨", "호흡", "기절", "의식", "심장", "흉통", "가슴", "마비", "출혈", "피가", "119", "응급"],
    dept: "응급의학과",
    desc: "즉시 응급 처치가 필요한 위급 상황입니다.",
    targetTypes: ["종합병원", "병원"],
    urgency: "EMERGENCY" as UrgencyLevel,
  },
  {
    keywords: ["밤", "새벽", "주말", "공휴일", "지금", "야간"],
    dept: "24시 진료/응급실",
    desc: "야간/휴일 진료가 가능한 병원을 우선합니다.",
    targetTypes: ["종합병원", "병원"],
    urgency: "URGENT" as UrgencyLevel,
  },
  {
    keywords: ["배", "소화", "토", "속", "체", "설사", "복통", "위", "장염"],
    dept: "내과",
    desc: "소화기 계통 문제",
    targetTypes: ["내과", "종합병원", "병원", "의원"],
    urgency: "NORMAL" as UrgencyLevel,
  },
  {
    keywords: ["뼈", "허리", "무릎", "관절", "다리", "팔", "골절", "근육", "통증", "어깨", "디스크"],
    dept: "정형외과",
    desc: "근골격계 질환",
    targetTypes: ["정형외과", "종합병원", "병원"],
    urgency: "NORMAL" as UrgencyLevel,
  },
  {
    keywords: ["눈", "시력", "충혈", "눈곱", "다래끼", "안구"],
    dept: "안과",
    desc: "안구 질환",
    targetTypes: ["안과", "종합병원"],
    urgency: "NORMAL" as UrgencyLevel,
  },
  {
    keywords: ["이", "치아", "잇몸", "사랑니", "스케일링", "턱", "치통"],
    dept: "치과",
    desc: "구강 질환",
    targetTypes: ["치과병원", "치과"],
    urgency: "NORMAL" as UrgencyLevel,
  },
  {
    keywords: ["피부", "두드러기", "가려움", "발진", "아토피", "여드름", "화상"],
    dept: "피부과",
    desc: "피부 질환",
    targetTypes: ["피부과", "종합병원", "병원"],
    urgency: "NORMAL" as UrgencyLevel,
  },
  {
    keywords: ["코", "목", "감기", "기침", "콧물", "귀", "청력", "비염"],
    dept: "이비인후과",
    desc: "호흡기/이비인후과 질환",
    targetTypes: ["이비인후과", "종합병원", "병원", "내과"],
    urgency: "NORMAL" as UrgencyLevel,
  },
  {
    keywords: ["열", "몸살", "오한", "두통", "독감"],
    dept: "내과",
    desc: "전신 증상 및 고열",
    targetTypes: ["내과", "종합병원", "병원", "의원"],
    urgency: "URGENT" as UrgencyLevel,
  },
  {
    keywords: ["침", "한약", "체질", "부항"],
    dept: "한방과",
    desc: "한방 진료",
    targetTypes: ["한방병원", "한의원"],
    urgency: "NORMAL" as UrgencyLevel,
  },
];

// --- [메인 컴포넌트 시작] ---
export default function AiHospitalSearch({ onSelectHospital }: AiHospitalSearchProps) {
  const router = useRouter(); 
  const [symptom, setSymptom] = useState(""); 
  const [isAnalyzing, setIsAnalyzing] = useState(false); 
  const [dbHospitals, setDbHospitals] = useState<any[]>([]); 

  const [result, setResult] = useState<{
    departmentTitle: string;
    desc: string;
    matchedHospitals: any[];
    urgencyLevel: UrgencyLevel;
    isComplex: boolean;
  } | null>(null);

  // ✅ [수정 포인트 1] 데이터 로드 및 좌표 변환 로직 보강
  useEffect(() => {
    const loadAndGeocode = async () => {
      try {
        const res = await hospitalService.getHospitals();
        const rawHospitals = res.data;

        // 지도가 로드될 때까지 기다렸다가 지오코딩을 수행하는 내부 함수
        const performGeocoding = () => {
          // window.kakao.maps.services까지 완벽히 로드되었는지 확인
          if (window.kakao && window.kakao.maps && window.kakao.maps.services) {
            const geocoder = new window.kakao.maps.services.Geocoder();

            const promises = rawHospitals.map((item: any) => {
              return new Promise((resolve) => {
                if (!item.address) { resolve(null); return; }
                geocoder.addressSearch(item.address, (result, status) => {
                  if (status === window.kakao.maps.services.Status.OK) {
                    resolve({
                      ...item,
                      lat: Number(result[0].y),
                      lng: Number(result[0].x),
                    });
                  } else { resolve(null); }
                });
              });
            });

            Promise.all(promises).then((results) => {
              setDbHospitals(results.filter((h) => h !== null));
            });
          } else {
            // 아직 로드가 안 됐다면 0.5초 후 재시도 (에러 방지 핵심)
            setTimeout(performGeocoding, 500);
          }
        };

        performGeocoding();
      } catch (err) {
        console.error("AI 상담소 데이터 로드 실패:", err);
      }
    };

    loadAndGeocode();
  }, []);

  // 2. [분석 로직] - 기존 로직 유지
  const handleAnalyze = () => {
    if (!symptom.trim()) return;

    setIsAnalyzing(true);
    setResult(null);

    setTimeout(() => {
      const text = symptom;
      const matchedRules = ANALYSIS_RULES.filter((rule) =>
        rule.keywords.some((keyword) => text.includes(keyword))
      );

      let displayTitle = "";
      let displayDesc = "";
      let finalTargetTypes = new Set<string>();
      let keywordsToFilter = new Set<string>();
      let maxUrgency: UrgencyLevel = "NORMAL";

      if (matchedRules.length === 0) {
        displayTitle = "가까운 병원";
        displayDesc = "증상을 명확히 파악하기 어려워 일반 진료 병원을 추천합니다.";
        ["종합병원", "병원", "의원", "내과"].forEach((t) => finalTargetTypes.add(t));
      } else {
        const hasEmergency = matchedRules.some((r) => r.urgency === "EMERGENCY");
        const hasUrgent = matchedRules.some((r) => r.urgency === "URGENT");

        if (hasEmergency) maxUrgency = "EMERGENCY";
        else if (hasUrgent) maxUrgency = "URGENT";

        const depts = matchedRules.filter((r) => r.dept !== "24시 진료/응급실").map((r) => r.dept);
        const uniqueDepts = Array.from(new Set(depts)).join(", ");

        if (maxUrgency === "EMERGENCY") {
          displayTitle = "응급 상황 감지";
          displayDesc = "즉시 처치가 가능한 종합병원 및 응급의료기관을 추천합니다.";
          finalTargetTypes.add("종합병원");
        } else if (maxUrgency === "URGENT") {
          displayTitle = uniqueDepts ? `${uniqueDepts} (야간/진료가능)` : "야간/휴일 진료";
          displayDesc = "현재 진료 가능성이 높은 대형 병원을 우선 추천합니다.";
          finalTargetTypes.add("종합병원");
        } else {
          displayTitle = uniqueDepts;
          displayDesc = matchedRules.length > 1
              ? "여러 증상이 복합되어 종합적인 진료가 필요해 보입니다."
              : matchedRules[0].desc + " 관련 전문 병원을 우선 추천합니다.";
        }

        matchedRules.forEach((rule) => {
          rule.targetTypes.forEach((t) => finalTargetTypes.add(t));
          if (rule.dept !== "24시 진료/응급실" && rule.dept !== "응급의학과") {
            keywordsToFilter.add(rule.dept.split("/")[0]);
          }
        });
      }

      let filtered = dbHospitals.filter((h) => {
        if (!h.lat || !h.lng) return false;
        const category = (h.treatCategory || h.type || "").toString();
        const name = (h.name || "").toString();
        const isTypeMatch = Array.from(finalTargetTypes).some((t) => category.includes(t));
        const isNameMatch = Array.from(keywordsToFilter).some((k) => name.includes(k));

        if (keywordsToFilter.has("치과") && !keywordsToFilter.has("내과")) {
          return category.includes("치과") || name.includes("치과");
        }
        return isTypeMatch || isNameMatch;
      });

      filtered.sort((a, b) => {
        let scoreA = 0, scoreB = 0;
        keywordsToFilter.forEach((k) => {
          if (a.name.includes(k)) scoreA += 3;
          if (b.name.includes(k)) scoreB += 3;
        });
        return scoreB - scoreA;
      });

      setResult({
        departmentTitle: displayTitle,
        desc: displayDesc,
        matchedHospitals: filtered.slice(0, 5),
        urgencyLevel: maxUrgency,
        isComplex: matchedRules.length > 1,
      });
      setIsAnalyzing(false);
    }, 1500);
  };

  const handleReset = () => {
    setSymptom("");
    setResult(null);
  };

  const getThemeColor = (urgency: UrgencyLevel) => {
    switch (urgency) {
      case "EMERGENCY":
        return { bg: "bg-red-50", border: "border-red-500", text: "text-red-700", badge: "bg-red-100 text-red-800", hoverBorder: "hover:border-red-400", bar: "bg-red-500", icon: <Siren size={18} className="text-red-600 animate-pulse" /> };
      case "URGENT":
        return { bg: "bg-amber-50", border: "border-amber-400", text: "text-amber-800", badge: "bg-amber-100 text-amber-900", hoverBorder: "hover:border-amber-400", bar: "bg-amber-500", icon: <Clock size={18} className="text-amber-600" /> };
      default:
        return { bg: "bg-white", border: "border-green-500", text: "text-green-600", badge: "bg-green-100 text-green-700", hoverBorder: "hover:border-green-400", bar: "bg-green-500", icon: <CheckCircle2 size={18} className="text-green-600" /> };
    }
  };

  return (
    <div className="bg-gradient-to-br from-slate-50 to-white rounded-[2.5rem] p-6 border border-slate-100 shadow-sm relative overflow-hidden group">
      <div className="absolute -top-10 -right-10 w-32 h-32 bg-green-100 rounded-full blur-3xl opacity-50 group-hover:opacity-100 transition-opacity" />

      <div className="relative z-10">
        <div className="flex items-center gap-2 mb-4">
          <div className="p-2 bg-green-100 text-green-600 rounded-xl">
            <Sparkles size={18} className={isAnalyzing ? "animate-spin" : ""} />
          </div>
          <div>
            <h3 className="text-lg font-black text-slate-800 leading-tight">AI 증상 상담소</h3>
            <p className="text-xs text-slate-400 font-medium">증상과 상황을 말하면 딱 맞는 병원을 추천해요.</p>
          </div>
        </div>

        {!result ? (
          <div className="space-y-3">
            <textarea
              value={symptom}
              onChange={(e) => setSymptom(e.target.value)}
              placeholder="예: 숨쉬기가 힘들어요 / 밤인데 배가 아파요"
              className="w-full h-24 p-4 bg-white border border-slate-200 rounded-2xl text-sm font-medium focus:outline-none focus:ring-2 focus:ring-green-500/20 focus:border-green-500 resize-none transition-all placeholder:text-slate-300"
              disabled={isAnalyzing}
            />
            <button
              onClick={handleAnalyze}
              disabled={!symptom || isAnalyzing}
              className={`w-full py-3.5 rounded-xl font-bold text-sm flex items-center justify-center gap-2 transition-all shadow-lg ${!symptom || isAnalyzing ? "bg-slate-100 text-slate-400 cursor-not-allowed shadow-none" : "bg-slate-900 text-white hover:bg-green-600 hover:shadow-green-200"}`}
            >
              {isAnalyzing ? <><Activity size={16} className="animate-pulse" /> 증상 분석중...</> : <><Search size={16} /> 병원 찾아보기</>}
            </button>
          </div>
        ) : (
          <div className="animate-in fade-in slide-in-from-bottom-2 duration-300">
            <div className={`border-2 rounded-2xl p-5 mb-4 shadow-md relative ${getThemeColor(result.urgencyLevel).bg} ${getThemeColor(result.urgencyLevel).border}`}>
              <button onClick={handleReset} className="absolute top-4 right-4 text-slate-400 hover:text-slate-600"><X size={16} /></button>
              <div className="flex items-center gap-2 mb-2">
                <span className={`text-[10px] font-bold px-2 py-0.5 rounded-md flex items-center gap-1 ${getThemeColor(result.urgencyLevel).badge}`}>
                  {getThemeColor(result.urgencyLevel).icon} {result.urgencyLevel === "EMERGENCY" ? "응급 상황" : "AI 분석 완료"}
                </span>
              </div>
              <h4 className={`text-xl font-black mb-1 leading-snug ${getThemeColor(result.urgencyLevel).text}`}>{result.departmentTitle}</h4>
              <p className="text-xs text-slate-600 font-medium leading-relaxed mb-0">{result.desc}</p>
            </div>

            <div className="mb-4">
              <h5 className="text-sm font-bold text-slate-800 flex items-center gap-1 mb-3 px-1">추천 병원 TOP {result.matchedHospitals.length}</h5>
              <div className="space-y-2 pr-1">
                {result.matchedHospitals.map((h, idx) => (
                  <div
                    key={h.id}
                    onClick={() => { if (onSelectHospital) onSelectHospital(h); }}
                    className={`bg-white p-3 rounded-xl border border-slate-100 hover:shadow-md transition-all cursor-pointer flex justify-between items-center group relative overflow-hidden ${getThemeColor(result.urgencyLevel).hoverBorder}`}
                  >
                    {idx === 0 && <div className={`absolute top-0 left-0 w-1 h-full ${getThemeColor(result.urgencyLevel).bar}`} />}
                    <div className="overflow-hidden flex-1 pl-2 pr-2">
                      <div className="flex justify-between items-start mb-0.5">
                        <p className="text-xs font-bold text-slate-800 truncate transition-colors flex-1 group-hover:text-slate-900">{h.name}</p>
                        <span className="text-[9px] bg-slate-100 text-slate-500 px-1.5 py-0.5 rounded ml-2 shrink-0">{h.treatCategory || h.type || "병원"}</span>
                      </div>
                      <p className="text-[10px] text-slate-400 truncate flex items-center gap-1"><MapPin size={10} /> {h.address}</p>
                    </div>
                    <button
                      onClick={(e) => { e.stopPropagation(); router.push(`/hospital/${h.id}`); }}
                      className="flex items-center gap-1 px-2.5 py-1.5 bg-slate-50 hover:bg-slate-100 text-slate-400 hover:text-green-600 rounded-lg transition-colors text-[10px] font-bold border border-slate-100 shrink-0"
                    >상세 <ExternalLink size={10} /></button>
                  </div>
                ))}
              </div>
            </div>
            <button onClick={handleReset} className="w-full py-3 bg-slate-100 text-slate-600 rounded-xl text-xs font-bold hover:bg-slate-200 transition-colors flex items-center justify-center gap-1">다시 검색하기</button>
          </div>
        )}
      </div>
    </div>
  );
}