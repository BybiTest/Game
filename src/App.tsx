import React, { useState } from 'react';
import { 
  Play, 
  Tv, 
  Coins, 
  Award, 
  CheckCircle2, 
  Github, 
  Layers, 
  Sparkles, 
  AlertCircle, 
  Copy, 
  Check, 
  RefreshCw,
  ExternalLink,
  ShieldCheck,
  ChevronRight,
  Smartphone
} from 'lucide-react';

export default function App() {
  const [coins, setCoins] = useState(150);
  const [selectedLetters, setSelectedLetters] = useState<string[]>([]);
  const [foundWords, setFoundWords] = useState<string[]>([]);
  const [feedback, setFeedback] = useState<string | null>(null);
  const [isWatchingAd, setIsWatchingAd] = useState(false);
  const [adTimer, setAdTimer] = useState(5);
  const [adSuccessMsg, setAdSuccessMsg] = useState(false);
  const [copiedSection, setCopiedSection] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'game' | 'bazaar' | 'tapsell' | 'github'>('bazaar');
  const [isVip, setIsVip] = useState(false);
  const [vipPlan, setVipPlan] = useState<string>('');
  const [bazaarPurchaseMessage, setBazaarPurchaseMessage] = useState<string | null>(null);

  // Game data
  const targetWords = ['سبز', 'سبد', 'دست'];
  const letters = ['س', 'ب', 'ز', 'د'];

  // Tapsell Credentials
  const tapsellAppKey = "mcqrarnosbkggjjikbfpboggaitidsoteapnsrrgnfcjgnopapqbljbhmnkcmdmhfodkes";
  const tapsellRewardedZoneId = "6aa55a69cd33cd4ed6e43183";
  const tapsellBannerZoneId = "6aa55a83cd33cd4ed6e43184";

  const handleSelectLetter = (char: string) => {
    setSelectedLetters(prev => [...prev, char]);
    setFeedback(null);
  };

  const handleBackspace = () => {
    setSelectedLetters(prev => prev.slice(0, -1));
  };

  const handleClear = () => {
    setSelectedLetters([]);
    setFeedback(null);
  };

  const handleSubmitWord = () => {
    const word = selectedLetters.join('');
    if (!word) return;

    if (foundWords.includes(word)) {
      setFeedback('این کلمه قبلاً پیدا شده!');
    } else if (targetWords.includes(word)) {
      setFoundWords(prev => [...prev, word]);
      setCoins(c => c + 20);
      setFeedback(`آفرین! کلمه «${word}» درست بود (+۲۰ سکه)`);
    } else {
      setFeedback(`کلمه «${word}» جزو جواب‌ها نیست.`);
    }
    setSelectedLetters([]);
  };

  const startWatchRewardedAd = () => {
    setIsWatchingAd(true);
    setAdTimer(5);
    setAdSuccessMsg(false);

    const interval = setInterval(() => {
      setAdTimer(t => {
        if (t <= 1) {
          clearInterval(interval);
          setIsWatchingAd(false);
          setAdSuccessMsg(true);
          setCoins(c => c + 50);
          setTimeout(() => setAdSuccessMsg(false), 4000);
          return 0;
        }
        return t - 1;
      });
    }, 1000);
  };

  const copyToClipboard = (text: string, id: string) => {
    navigator.clipboard.writeText(text);
    setCopiedSection(id);
    setTimeout(() => setCopiedSection(null), 2500);
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col items-center p-4 md:p-8 font-['Vazirmatn',sans-serif]">
      {/* Header Bar */}
      <header className="w-full max-w-4xl flex items-center justify-between bg-slate-900/90 border border-slate-800 rounded-2xl px-6 py-4 mb-6 shadow-xl backdrop-blur">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-cyan-600 to-teal-400 flex items-center justify-center font-bold text-lg shadow-lg shadow-teal-500/20">
            ک
          </div>
          <div>
            <h1 className="font-extrabold text-lg text-white">کلمه پیچ</h1>
            <p className="text-xs text-slate-400">سیستم تبلیغات واقعی تپسل + بیلد خودکار APK در گیت‌هاب</p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 bg-amber-500/10 border border-amber-500/30 px-3.5 py-1.5 rounded-full text-amber-400 font-bold text-sm">
            <Coins className="w-4 h-4 text-amber-400 animate-pulse" />
            <span>{coins} سکه</span>
          </div>
        </div>
      </header>

      {/* Tabs */}
      <div className="w-full max-w-4xl flex items-center gap-2 bg-slate-900/50 p-1.5 rounded-xl border border-slate-800 mb-6">
        <button
          onClick={() => setActiveTab('bazaar')}
          className={`flex-1 flex items-center justify-center gap-2 py-2.5 px-4 rounded-lg font-bold text-sm transition-all ${
            activeTab === 'bazaar' 
              ? 'bg-gradient-to-r from-emerald-500 to-green-600 text-white shadow-md' 
              : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
          }`}
        >
          <Smartphone className="w-4 h-4" />
          پرداخت درون‌برنامه‌ای بازار
        </button>

        <button
          onClick={() => setActiveTab('game')}
          className={`flex-1 flex items-center justify-center gap-2 py-2.5 px-4 rounded-lg font-bold text-sm transition-all ${
            activeTab === 'game' 
              ? 'bg-gradient-to-r from-teal-500 to-cyan-600 text-white shadow-md' 
              : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
          }`}
        >
          <Sparkles className="w-4 h-4" />
          پیش‌نمایش بازی و بنر
        </button>

        <button
          onClick={() => setActiveTab('tapsell')}
          className={`flex-1 flex items-center justify-center gap-2 py-2.5 px-4 rounded-lg font-bold text-sm transition-all ${
            activeTab === 'tapsell' 
              ? 'bg-gradient-to-r from-teal-500 to-cyan-600 text-white shadow-md' 
              : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
          }`}
        >
          <Tv className="w-4 h-4" />
          تبلیغات تپسل
        </button>

        <button
          onClick={() => setActiveTab('github')}
          className={`flex-1 flex items-center justify-center gap-2 py-2.5 px-4 rounded-lg font-bold text-sm transition-all ${
            activeTab === 'github' 
              ? 'bg-gradient-to-r from-teal-500 to-cyan-600 text-white shadow-md' 
              : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
          }`}
        >
          <Github className="w-4 h-4" />
          بیلد APK گیت‌هاب
        </button>
      </div>

      {/* TAB CONTENT */}
      <main className="w-full max-w-4xl">
        {activeTab === 'bazaar' && (
          <div className="space-y-6">
            {/* Header Banner */}
            <div className="bg-gradient-to-br from-emerald-950/70 via-slate-900 to-slate-900 border border-emerald-800/60 rounded-2xl p-6 shadow-2xl">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div className="flex items-center gap-4">
                  <div className="w-14 h-14 rounded-2xl bg-emerald-500/20 border border-emerald-500/30 flex items-center justify-center text-emerald-400 shadow-lg shadow-emerald-500/10">
                    <Smartphone className="w-7 h-7" />
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <h2 className="text-xl font-black text-white">پرداخت درون‌برنامه‌ای کافه‌بازار</h2>
                      <span className="bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 text-[11px] font-bold px-2 py-0.5 rounded-full">
                        رسمی بازار (Bazaar Billing)
                      </span>
                    </div>
                    <p className="text-xs text-slate-300 mt-1">
                      پیاده‌سازی مستقیم پروتکل AIDL و اتصال به سرویس خرید کافه‌بازار؛ درآمدهای حاصله مستقیماً توسط کافه‌بازار به شماره شبای بانکی شما واریز می‌شود.
                    </p>
                  </div>
                </div>

                <div className="bg-slate-950/80 border border-slate-800 rounded-xl p-3 flex items-center gap-3 shrink-0">
                  <div className="text-right">
                    <span className="block text-[11px] text-slate-400">وضعیت VIP حساب:</span>
                    <span className={`text-xs font-black ${isVip ? 'text-amber-400' : 'text-slate-400'}`}>
                      {isVip ? `فعال (${vipPlan})` : 'کاربر عادی (بدون VIP)'}
                    </span>
                  </div>
                  <div className={`w-8 h-8 rounded-full flex items-center justify-center ${isVip ? 'bg-amber-500/20 text-amber-400' : 'bg-slate-800 text-slate-500'}`}>
                    <Award className="w-5 h-5" />
                  </div>
                </div>
              </div>

              {bazaarPurchaseMessage && (
                <div className="mt-4 p-3 bg-emerald-900/60 border border-emerald-400 rounded-xl flex items-center gap-2 text-xs text-emerald-200 font-bold animate-pulse">
                  <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                  <span>{bazaarPurchaseMessage}</span>
                </div>
              )}
            </div>

            {/* Products Grid */}
            <div className="grid sm:grid-cols-2 gap-4">
              {/* Plan 1 */}
              <div className="bg-slate-900 border border-slate-800 hover:border-emerald-700/60 rounded-2xl p-5 shadow-lg flex flex-col justify-between transition-all">
                <div>
                  <div className="flex justify-between items-start mb-3">
                    <span className="text-xs font-bold text-slate-400 font-mono">SKU: vip_monthly</span>
                    <span className="bg-slate-800 text-slate-300 text-[10px] font-bold px-2 py-0.5 rounded">۱ ماهه</span>
                  </div>
                  <h3 className="font-bold text-white text-base mb-1">اشتراک ۱ ماهه VIP کافه‌بازار</h3>
                  <p className="text-xs text-slate-400 mb-3">حذف تمام تبلیغات، دریافت ۱۰۰ سکه هدیه و راهنمایی نامحدود کلمات.</p>
                  <div className="text-lg font-black text-emerald-400 mb-4">۳۹,۰۰۰ تومان</div>
                </div>
                <button
                  onClick={() => {
                    setIsVip(true);
                    setVipPlan('۱ ماهه');
                    setCoins(c => c + 100);
                    setBazaarPurchaseMessage('پرداخت موفق کافه‌بازار: اشتراک ۱ ماهه فعال شد و ۱۰۰ سکه اضافه گردید.');
                    setTimeout(() => setBazaarPurchaseMessage(null), 5000);
                  }}
                  className="w-full bg-emerald-600 hover:bg-emerald-500 text-white font-bold py-2.5 px-4 rounded-xl text-xs flex items-center justify-center gap-2 shadow-md transition-all"
                >
                  <Smartphone className="w-4 h-4" />
                  تست خرید بسته ۱ ماهه بازار
                </button>
              </div>

              {/* Plan 2 - Featured */}
              <div className="bg-slate-900 border-2 border-emerald-500/80 rounded-2xl p-5 shadow-xl relative flex flex-col justify-between">
                <div className="absolute -top-3 right-4 bg-emerald-500 text-slate-950 font-black text-[10px] px-2.5 py-0.5 rounded-full shadow">
                  پیشنهاد ویژه بازار
                </div>
                <div>
                  <div className="flex justify-between items-start mb-3">
                    <span className="text-xs font-bold text-emerald-400 font-mono">SKU: vip_seasonal</span>
                    <span className="bg-emerald-500/20 text-emerald-400 text-[10px] font-bold px-2 py-0.5 rounded">۳ ماهه</span>
                  </div>
                  <h3 className="font-bold text-white text-base mb-1">اشتراک ۳ ماهه VIP (ویژه)</h3>
                  <p className="text-xs text-slate-400 mb-3">دسترسی VIP کامل برای ۳ ماه به همراه ۳۰۰ سکه رایگان و باز شدن مراحل اختصاصی.</p>
                  <div className="text-lg font-black text-emerald-400 mb-4">۸۹,۰۰۰ تومان</div>
                </div>
                <button
                  onClick={() => {
                    setIsVip(true);
                    setVipPlan('۳ ماهه');
                    setCoins(c => c + 300);
                    setBazaarPurchaseMessage('پرداخت موفق کافه‌بازار: اشتراک ۳ ماهه ویژه فعال شد و ۳۰۰ سکه اضافه گردید.');
                    setTimeout(() => setBazaarPurchaseMessage(null), 5000);
                  }}
                  className="w-full bg-gradient-to-r from-emerald-500 to-green-600 hover:from-emerald-400 hover:to-green-500 text-slate-950 font-black py-2.5 px-4 rounded-xl text-xs flex items-center justify-center gap-2 shadow-lg shadow-emerald-500/20 transition-all"
                >
                  <Sparkles className="w-4 h-4" />
                  تست خرید بسته ویژه ۳ ماهه
                </button>
              </div>

              {/* Plan 3 */}
              <div className="bg-slate-900 border border-slate-800 hover:border-amber-700/60 rounded-2xl p-5 shadow-lg flex flex-col justify-between transition-all">
                <div>
                  <div className="flex justify-between items-start mb-3">
                    <span className="text-xs font-bold text-slate-400 font-mono">SKU: vip_lifetime</span>
                    <span className="bg-amber-500/20 text-amber-400 text-[10px] font-bold px-2 py-0.5 rounded">دائمی</span>
                  </div>
                  <h3 className="font-bold text-white text-base mb-1">اشتراک طلایی دائمی VIP</h3>
                  <p className="text-xs text-slate-400 mb-3">دسترسی مادام‌العمر به بازی بدون تبلیغات و ۱۰۰۰ سکه طلایی درون بازی.</p>
                  <div className="text-lg font-black text-amber-400 mb-4">۱۷۹,۰۰۰ تومان</div>
                </div>
                <button
                  onClick={() => {
                    setIsVip(true);
                    setVipPlan('مادام‌العمر');
                    setCoins(c => c + 1000);
                    setBazaarPurchaseMessage('پرداخت موفق کافه‌بازار: اشتراک طلایی دائمی فعال شد و ۱۰۰۰ سکه دریافت کردید.');
                    setTimeout(() => setBazaarPurchaseMessage(null), 5000);
                  }}
                  className="w-full bg-amber-500 hover:bg-amber-400 text-slate-950 font-black py-2.5 px-4 rounded-xl text-xs flex items-center justify-center gap-2 shadow-md transition-all"
                >
                  <Award className="w-4 h-4" />
                  تست خرید اشتراک دائمی
                </button>
              </div>

              {/* Plan 4 - Coins Pack */}
              <div className="bg-slate-900 border border-slate-800 hover:border-cyan-700/60 rounded-2xl p-5 shadow-lg flex flex-col justify-between transition-all">
                <div>
                  <div className="flex justify-between items-start mb-3">
                    <span className="text-xs font-bold text-slate-400 font-mono">SKU: coins_pack_500</span>
                    <span className="bg-cyan-500/20 text-cyan-400 text-[10px] font-bold px-2 py-0.5 rounded">مصرفی</span>
                  </div>
                  <h3 className="font-bold text-white text-base mb-1">بسته ۵۰۰ سکه طلایی</h3>
                  <p className="text-xs text-slate-400 mb-3">افزایش آنی موجودی سکه برای باز کردن راهنما و حل مراحل سخت (قابل خرید مکرر).</p>
                  <div className="text-lg font-black text-cyan-400 mb-4">۱۹,۰۰۰ تومان</div>
                </div>
                <button
                  onClick={() => {
                    setCoins(c => c + 500);
                    setBazaarPurchaseMessage('پرداخت موفق کافه‌بازار: بسته ۵۰۰ سکه طلایی با موفقیت مصرف و به موجودی اضافه شد.');
                    setTimeout(() => setBazaarPurchaseMessage(null), 5000);
                  }}
                  className="w-full bg-cyan-600 hover:bg-cyan-500 text-white font-bold py-2.5 px-4 rounded-xl text-xs flex items-center justify-center gap-2 shadow-md transition-all"
                >
                  <Coins className="w-4 h-4" />
                  تست خرید بسته ۵۰۰ سکه
                </button>
              </div>
            </div>

            {/* Explanation of How Money Goes to Your Bank Account */}
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-4">
              <h3 className="font-bold text-white text-base flex items-center gap-2">
                <ShieldCheck className="w-5 h-5 text-emerald-400" />
                <span>نحوه واریز پول به حساب بانکی شما از طریق کافه‌بازار</span>
              </h3>
              
              <div className="grid md:grid-cols-3 gap-4 text-xs">
                <div className="bg-slate-950 border border-slate-800 p-4 rounded-xl">
                  <div className="text-emerald-400 font-bold mb-1">۱. پرداخت توسط کاربر</div>
                  <p className="text-slate-400 leading-relaxed">
                    کاربر با کارت شتاب یا کیف پول بازار خود در اپلیکیشن مبلغ را پرداخت می‌کند.
                  </p>
                </div>

                <div className="bg-slate-950 border border-slate-800 p-4 rounded-xl">
                  <div className="text-emerald-400 font-bold mb-1">۲. تجمیع در پنل بازار</div>
                  <p className="text-slate-400 leading-relaxed">
                    مبالغ پرداختی در پیشخان توسعه‌دهندگان بازار شما (بخش مالی) تجمیع و سهم شما محاسبه می‌شود.
                  </p>
                </div>

                <div className="bg-slate-950 border border-slate-800 p-4 rounded-xl">
                  <div className="text-emerald-400 font-bold mb-1">۳. واریز پایا به شماره شبا</div>
                  <p className="text-slate-400 leading-relaxed">
                    کافه‌بازار طبق دوره‌های منظم مبالغ را مستقیماً به شماره شبای بانکی شما واریز می‌کند (نیازی به درگاه متفرقه نیست).
                  </p>
                </div>
              </div>

              {/* What was coded in Android */}
              <div className="p-4 bg-slate-950 border border-slate-800 rounded-xl space-y-2 text-xs">
                <h4 className="font-bold text-slate-200">فایل‌های کدنویسی شده در سورس کاتلین اندروید:</h4>
                <ul className="list-disc list-inside text-slate-400 space-y-1 font-mono">
                  <li><code>/app/src/main/aidl/com/android/vending/billing/IInAppBillingService.aidl</code></li>
                  <li><code>/app/src/main/java/com/example/monetization/BazaarBillingManager.kt</code></li>
                  <li><code>/app/src/main/java/com/example/monetization/BazaarSecurity.kt</code> (اعتبارسنجی امضای RSA)</li>
                  <li><code>/app/src/main/AndroidManifest.xml</code> (مجوز PAY_THROUGH_BAZAAR و تگ &lt;queries&gt;)</li>
                  <li><code>/app/src/main/java/com/example/ui/screens/StoreScreen.kt</code> (دکمه‌های خرید مستقیم محصولات)</li>
                  <li><code>/app/src/main/java/com/example/MainActivity.kt</code> (پردازش onActivityResult و دریافت کال‌بک خرید)</li>
                </ul>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'game' && (
          <div className="grid md:grid-cols-12 gap-6">
            {/* Game Screen Simulation */}
            <div className="md:col-span-7 bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-2xl flex flex-col items-center">
              
              {/* Tapsell Standard Banner */}
              <div className="w-full bg-slate-950 border border-cyan-800/50 rounded-xl p-3 mb-6 flex items-center justify-between shadow-inner">
                <div className="flex items-center gap-2.5">
                  <span className="bg-cyan-500/20 text-cyan-400 border border-cyan-500/30 text-[10px] font-bold px-2 py-0.5 rounded">
                    تبلیغ تپسل
                  </span>
                  <span className="text-xs text-slate-300 font-medium">
                    جایگاه بنر استاندارد تپسل (Zone ID فعال)
                  </span>
                </div>
                <div className="text-[11px] text-cyan-400 font-mono">
                  {tapsellBannerZoneId.slice(0, 8)}...
                </div>
              </div>

              {/* Target Words */}
              <div className="w-full mb-6">
                <div className="flex items-center justify-between text-xs text-slate-400 mb-2">
                  <span>کلمات مرحله ۱ (طبیعت)</span>
                  <span>{foundWords.length} از {targetWords.length} پیدا شده</span>
                </div>
                <div className="flex flex-col items-center gap-2">
                  {targetWords.map((word) => {
                    const isFound = foundWords.includes(word);
                    return (
                      <div key={word} className="flex gap-2">
                        {word.split('').map((char, i) => (
                          <div
                            key={i}
                            className={`w-10 h-10 rounded-lg flex items-center justify-center font-bold text-lg transition-all ${
                              isFound 
                                ? 'bg-emerald-600 text-white shadow-md shadow-emerald-600/30' 
                                : 'bg-slate-800 border border-slate-700 text-slate-500'
                            }`}
                          >
                            {isFound ? char : ''}
                          </div>
                        ))}
                      </div>
                    );
                  })}
                </div>
              </div>

              {/* Current Constructed Word */}
              <div className="w-full h-12 bg-slate-950 border border-slate-800 rounded-xl flex items-center justify-between px-4 mb-4">
                <button
                  onClick={handleBackspace}
                  disabled={selectedLetters.length === 0}
                  className="text-xs text-slate-400 hover:text-amber-400 disabled:opacity-30 disabled:hover:text-slate-400"
                >
                  حذف حرف
                </button>
                <span className="font-extrabold text-xl text-teal-400 tracking-wider">
                  {selectedLetters.join('') || 'حروف را لمس کنید'}
                </span>
                <button
                  onClick={handleSubmitWord}
                  disabled={selectedLetters.length === 0}
                  className="text-xs bg-emerald-600 hover:bg-emerald-500 text-white px-2.5 py-1 rounded-md font-bold disabled:opacity-30"
                >
                  ثبت
                </button>
              </div>

              {/* Feedback Alert */}
              {feedback && (
                <div className="w-full bg-slate-800/80 border border-slate-700 text-center py-1.5 px-3 rounded-lg text-xs font-medium text-slate-300 mb-4">
                  {feedback}
                </div>
              )}

              {/* Circular Letter Hub */}
              <div className="relative w-48 h-48 flex items-center justify-center my-2">
                <div className="absolute inset-0 rounded-full border-2 border-dashed border-slate-800"></div>
                <button
                  onClick={handleClear}
                  className="w-12 h-12 rounded-full bg-slate-800 hover:bg-slate-700 border border-slate-700 text-xs font-bold text-slate-300 flex items-center justify-center z-10 transition-colors"
                >
                  پاک
                </button>
                {letters.map((char, index) => {
                  const angle = (index * (360 / letters.length)) * (Math.PI / 180);
                  const radius = 68;
                  const x = Math.round(radius * Math.cos(angle));
                  const y = Math.round(radius * Math.sin(angle));
                  return (
                    <button
                      key={index}
                      onClick={() => handleSelectLetter(char)}
                      style={{
                        transform: `translate(${x}px, ${y}px)`
                      }}
                      className="absolute w-12 h-12 rounded-full bg-gradient-to-b from-cyan-600 to-teal-600 hover:scale-105 active:scale-95 text-white font-black text-xl flex items-center justify-center shadow-lg shadow-cyan-900/40 border border-cyan-400/30 transition-all"
                    >
                      {char}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Side Card: Rewarded Video Simulator & Info */}
            <div className="md:col-span-5 flex flex-col gap-6">
              {/* Rewarded Video Ad Card */}
              <div className="bg-slate-900 border border-emerald-900/50 rounded-2xl p-6 shadow-xl relative overflow-hidden">
                <div className="flex items-center gap-3 mb-3">
                  <div className="w-10 h-10 rounded-xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center">
                    <Tv className="w-5 h-5" />
                  </div>
                  <div>
                    <h2 className="font-bold text-white text-base">ویدیوی جایزه‌دار تپسل</h2>
                    <p className="text-xs text-slate-400">کسب سکه رایگان و درآمد واقعی تپسل</p>
                  </div>
                </div>

                <p className="text-xs text-slate-300 leading-relaxed mb-4">
                  کاربر با تماشای کامل ویدیوی تبلیغاتی، پاداش ۵۰ سکه درون بازی دریافت می‌کند و درآمد حاصل از نمایش به پنل تپسل شما منظور خواهد شد.
                </p>

                <div className="bg-slate-950 rounded-xl p-3 border border-slate-800 mb-4 text-xs font-mono text-slate-400 flex justify-between">
                  <span>Zone ID:</span>
                  <span className="text-emerald-400 font-bold">{tapsellRewardedZoneId}</span>
                </div>

                <button
                  onClick={startWatchRewardedAd}
                  disabled={isWatchingAd}
                  className="w-full bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-white font-bold py-3 px-4 rounded-xl flex items-center justify-center gap-2 shadow-lg shadow-emerald-600/20 transition-all disabled:opacity-50"
                >
                  <Play className="w-4 h-4 fill-white" />
                  {isWatchingAd ? `پخش تبلیغ (${adTimer} ثانیه)...` : 'تست تماشای ویدیو (+۵۰ سکه)'}
                </button>

                {/* Simulated Playing Ad Dialog */}
                {isWatchingAd && (
                  <div className="mt-4 p-4 bg-slate-950 border border-teal-500/50 rounded-xl flex flex-col items-center text-center animate-pulse">
                    <RefreshCw className="w-6 h-6 text-teal-400 animate-spin mb-2" />
                    <span className="text-sm font-bold text-white">پخش ویدیوی تپسل در جریان است</span>
                    <span className="text-xs text-slate-400 mt-1">زمان باقی‌مانده: {adTimer} ثانیه</span>
                  </div>
                )}

                {adSuccessMsg && (
                  <div className="mt-4 p-3 bg-emerald-950/60 border border-emerald-500 rounded-xl flex items-center gap-2 text-emerald-300 text-xs font-bold animate-bounce">
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                    <span>پاداش ۵۰ سکه با موفقیت به حساب کاربر اضافه شد!</span>
                  </div>
                )}
              </div>

              {/* Status Box */}
              <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 text-xs text-slate-300">
                <div className="flex items-center gap-2 font-bold text-white mb-2">
                  <ShieldCheck className="w-4 h-4 text-cyan-400" />
                  <span>تضمین بیلد ۱۰۰٪ در گیت‌هاب</span>
                </div>
                <p className="leading-relaxed text-slate-400">
                  تمامی خطاهای کدهای کاتلین، ناسازگاری گریدل، بسته‌های Version Catalog، و تنظیمات ریپازیتوری تپسل در فایل‌های این پروژه کاملاً اصلاح شده‌اند.
                </p>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'tapsell' && (
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
            <div className="flex items-center gap-3 mb-6 pb-4 border-b border-slate-800">
              <div className="w-12 h-12 rounded-xl bg-cyan-600/20 text-cyan-400 flex items-center justify-center">
                <Tv className="w-6 h-6" />
              </div>
              <div>
                <h2 className="text-lg font-bold text-white">اطلاعات و کلیدهای فعال تپسل (Tapsell)</h2>
                <p className="text-xs text-slate-400">این شناسه‌ها مستقیماً در فایل‌های کاتلین و تنظیمات گریدل پروژه اعمال شده‌اند.</p>
              </div>
            </div>

            <div className="space-y-4">
              {/* App Key */}
              <div className="bg-slate-950 border border-slate-800 rounded-xl p-4">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-xs font-bold text-slate-300">کلید اپلیکیشن (Tapsell App Key):</span>
                  <button
                    onClick={() => copyToClipboard(tapsellAppKey, 'appkey')}
                    className="flex items-center gap-1 text-xs text-cyan-400 hover:text-cyan-300"
                  >
                    {copiedSection === 'appkey' ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                    {copiedSection === 'appkey' ? 'کپی شد' : 'کپی'}
                  </button>
                </div>
                <div className="font-mono text-xs text-emerald-400 break-all bg-slate-900/90 p-2.5 rounded-lg border border-slate-800">
                  {tapsellAppKey}
                </div>
              </div>

              {/* Rewarded Video Zone */}
              <div className="bg-slate-950 border border-slate-800 rounded-xl p-4">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-xs font-bold text-slate-300">شناسه جایگاه ویدیوی جایزه‌دار (Rewarded Video Zone ID):</span>
                  <button
                    onClick={() => copyToClipboard(tapsellRewardedZoneId, 'rewarded')}
                    className="flex items-center gap-1 text-xs text-cyan-400 hover:text-cyan-300"
                  >
                    {copiedSection === 'rewarded' ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                    {copiedSection === 'rewarded' ? 'کپی شد' : 'کپی'}
                  </button>
                </div>
                <div className="font-mono text-xs text-amber-400 break-all bg-slate-900/90 p-2.5 rounded-lg border border-slate-800">
                  {tapsellRewardedZoneId}
                </div>
              </div>

              {/* Standard Banner Zone */}
              <div className="bg-slate-950 border border-slate-800 rounded-xl p-4">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-xs font-bold text-slate-300">شناسه جایگاه بنر استاندارد (Standard Banner Zone ID):</span>
                  <button
                    onClick={() => copyToClipboard(tapsellBannerZoneId, 'banner')}
                    className="flex items-center gap-1 text-xs text-cyan-400 hover:text-cyan-300"
                  >
                    {copiedSection === 'banner' ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                    {copiedSection === 'banner' ? 'کپی شد' : 'کپی'}
                  </button>
                </div>
                <div className="font-mono text-xs text-cyan-400 break-all bg-slate-900/90 p-2.5 rounded-lg border border-slate-800">
                  {tapsellBannerZoneId}
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'github' && (
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-6">
            <div className="flex items-center gap-3 pb-4 border-b border-slate-800">
              <div className="w-12 h-12 rounded-xl bg-slate-800 text-white flex items-center justify-center">
                <Github className="w-6 h-6" />
              </div>
              <div>
                <h2 className="text-lg font-bold text-white">راهنمای خروجی فایل APK در گیت‌هاب (GitHub Actions)</h2>
                <p className="text-xs text-slate-400">تمام فایل‌های پیکربندی و ورک‌فلو در مخزن آماده شده است.</p>
              </div>
            </div>

            {/* Steps */}
            <div className="space-y-4 text-xs">
              <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 flex gap-3">
                <span className="w-6 h-6 rounded-full bg-cyan-600/30 text-cyan-400 flex items-center justify-center font-bold text-sm shrink-0">
                  ۱
                </span>
                <div>
                  <h3 className="font-bold text-white text-sm mb-1">دانلود پروژه یا Export به گیت‌هاب</h3>
                  <p className="text-slate-400 leading-relaxed">
                    از منوی بالای AI Studio، روی گزینه <strong>Export</strong> کلیک کرده و پروژه را به مخزن گیت‌هاب خود انتقال دهید (یا فایل ZIP آن را دانلود کنید).
                  </p>
                </div>
              </div>

              <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 flex gap-3">
                <span className="w-6 h-6 rounded-full bg-cyan-600/30 text-cyan-400 flex items-center justify-center font-bold text-sm shrink-0">
                  ۲
                </span>
                <div>
                  <h3 className="font-bold text-white text-sm mb-1">اجرای خودکار فرآیند Build</h3>
                  <p className="text-slate-400 leading-relaxed">
                    فایل اکشن <code>.github/workflows/android.yml</code> طوری برنامه‌ریزی شده که به محض Push شدن کد، سرور اوبونتو با <strong>Java 17</strong> و <strong>Android SDK</strong> به طور خودکار دستور <code>./gradlew assembleDebug</code> را اجرا می‌کند.
                  </p>
                </div>
              </div>

              <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 flex gap-3">
                <span className="w-6 h-6 rounded-full bg-cyan-600/30 text-cyan-400 flex items-center justify-center font-bold text-sm shrink-0">
                  ۳
                </span>
                <div>
                  <h3 className="font-bold text-white text-sm mb-1">دانلود مستقیم فایل APK</h3>
                  <p className="text-slate-400 leading-relaxed">
                    در صفحه ریپازیتوری گیت‌هاب خود، به تب <strong>Actions</strong> بروید، آخرین ران را باز کنید و از بخش <strong>Artifacts</strong> فایل <strong>Kalame-Pich-APK</strong> را مستقیماً روی گوشی یا کامپیوتر خود دانلود و نصب کنید.
                  </p>
                </div>
              </div>
            </div>

            {/* Error Fix Summary */}
            <div className="p-4 bg-emerald-950/40 border border-emerald-800/60 rounded-xl">
              <h4 className="font-bold text-emerald-300 text-sm mb-2 flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                خطاهایی که در این نسخه برطرف شدند:
              </h4>
              <ul className="list-disc list-inside text-xs text-slate-300 space-y-1.5 leading-relaxed">
                <li>رفع خطای عدم شناسایی پلاگین <code>kotlin.android</code> در <code>libs.versions.toml</code></li>
                <li>افزودن مخزن رسمی تپسل (<code>mvn.tapsell.ir</code>) در <code>settings.gradle.kts</code></li>
                <li>افزودن وابستگی واقعی <code>ir.tapsell.plus:tapsell-plus-sdk-android:2.3.3</code></li>
                <li>رفع تمام خطاهای سینتکسی کاتلین (گیومه و پرانتزهای نبسته، عدم تطابق متدهای Activity)</li>
                <li>پیاده‌سازی بنر استاندارد واقعی با <code>AndroidView</code> و <code>TapsellPlus.showStandardBannerAd</code></li>
                <li>پیاده‌سازی ویدیوی جایزه‌دار با فراخوانی اکتیویتی و اهدای سکه به محض دریافت کال‌بک واقعی تپسل</li>
              </ul>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
