package com.example.data

import com.example.model.Plugin

object DefaultPlugins {
    val initialPlugins = listOf(
        Plugin(
            id = "plugin_dark_mode",
            name = "Тёмная тема сайтов",
            description = "Принудительно окрашивает любой сайт в мягкие темные тона для защиты глаз в ночное время.",
            author = "Тилкайо Team",
            version = "1.2.0",
            iconName = "dark_mode",
            category = "Внешний вид",
            cssCode = """
                html, body {
                    background-color: #161719 !important;
                    color: #E2E4E9 !important;
                }
                p, span, h1, h2, h3, h4, h5, h6, li, a, div {
                    color: #D6D9E0 !important;
                    border-color: #2D3139 !important;
                }
                input, textarea, select {
                    background-color: #24272E !important;
                    color: #FFFFFF !important;
                    border: 1px solid #3E434F !important;
                }
                img, video, canvas, svg {
                    filter: brightness(0.85) contrast(1.05) !important;
                }
            """.trimIndent(),
            jsCode = """
                console.log("[Тилкайо] Dark mode activated!");
            """.trimIndent(),
            matchUrlRegex = ".*",
            isEnabled = true,
            isPreinstalled = true,
            downloads = 1420L
        ),
        Plugin(
            id = "plugin_ad_blocker",
            name = "Блокировщик рекламы",
            description = "Скрывает назойливые рекламные баннеры, всплывающие окна и раздражающие оверлеи.",
            author = "Тилкайо Team",
            version = "2.1.0",
            iconName = "security",
            category = "Безопасность",
            cssCode = """
                .adsbygoogle, [class*="ad-banner"], [class*="advertisement"],
                [id*="ad_container"], [id*="banner-ad"], [class*="popup-banner"],
                div[data-ad-slot], iframe[src*="doubleclick.net"],
                iframe[src*="adservice"], [class*="sponsor-box"] {
                    display: none !important;
                    visibility: hidden !important;
                    height: 0 !important;
                    pointer-events: none !important;
                }
            """.trimIndent(),
            jsCode = """
                (function() {
                    const blockAds = () => {
                        const badSelectors = [
                            '.adsbygoogle', '[class*="ad-banner"]', '[id*="ad_container"]',
                            'div[data-ad-slot]', '[class*="advertisement"]'
                        ];
                        badSelectors.forEach(s => {
                            document.querySelectorAll(s).forEach(el => el.remove());
                        });
                    };
                    blockAds();
                    setTimeout(blockAds, 1000);
                    setTimeout(blockAds, 3000);
                })();
            """.trimIndent(),
            matchUrlRegex = ".*",
            isEnabled = true,
            isPreinstalled = true,
            downloads = 2850L
        ),
        Plugin(
            id = "plugin_ai_reader",
            name = "AI-пересказ и переводчик",
            description = "Умный помощник для чтения: добавляет кнопку на страницу для мгновенного анализа текста через Тилкайо.",
            author = "Тилкайо Team",
            version = "1.0.0",
            iconName = "auto_awesome",
            category = "ИИ",
            cssCode = """
                #tilcayo-reader-fab {
                    position: fixed !important;
                    bottom: 24px !important;
                    right: 20px !important;
                    z-index: 999999 !important;
                    background: linear-gradient(135deg, #E2882B, #C26815) !important;
                    color: #FFFFFF !important;
                    border: 2px solid #FFF2E0 !important;
                    border-radius: 28px !important;
                    padding: 10px 16px !important;
                    font-size: 13px !important;
                    font-weight: 600 !important;
                    box-shadow: 0 4px 14px rgba(0,0,0,0.3) !important;
                    cursor: pointer !important;
                    font-family: sans-serif !important;
                    display: flex !important;
                    align-items: center !important;
                    gap: 6px !important;
                }
            """.trimIndent(),
            jsCode = """
                (function() {
                    if (document.getElementById('tilcayo-reader-fab')) return;
                    const btn = document.createElement('div');
                    btn.id = 'tilcayo-reader-fab';
                    btn.innerHTML = '🐱 Тилкайо Перескажи';
                    btn.onclick = () => {
                        const pageText = (document.body.innerText || '').slice(0, 3000);
                        if (window.TilcayoBridge) {
                            window.TilcayoBridge.requestPageSummary(pageText);
                        } else {
                            alert("Тилкайо читает страницу... " + pageText.slice(0, 150) + "...");
                        }
                    };
                    document.body.appendChild(btn);
                })();
            """.trimIndent(),
            matchUrlRegex = "^https?://.*",
            isEnabled = true,
            isPreinstalled = true,
            downloads = 3100L
        ),
        Plugin(
            id = "plugin_stats_hud",
            name = "Инспектор чтения и слов",
            description = "Показывает в верхнем углу страницы количество слов и примерное время чтения статьи.",
            author = "Тилкайо Team",
            version = "1.0.0",
            iconName = "timer",
            category = "Утилиты",
            pluginType = "SCRIPT",
            cssCode = """
                #tilcayo-word-counter {
                    position: fixed !important;
                    top: 10px !important;
                    right: 10px !important;
                    background: rgba(30, 32, 38, 0.85) !important;
                    backdrop-filter: blur(8px) !important;
                    color: #FFC078 !important;
                    font-size: 11px !important;
                    font-family: monospace !important;
                    padding: 4px 10px !important;
                    border-radius: 12px !important;
                    border: 1px solid rgba(255, 192, 120, 0.3) !important;
                    z-index: 999990 !important;
                    pointer-events: none !important;
                }
            """.trimIndent(),
            jsCode = """
                (function() {
                    if (document.getElementById('tilcayo-word-counter')) return;
                    const text = document.body.innerText || '';
                    const words = text.trim().split(/\\s+/).filter(Boolean).length;
                    const minutes = Math.max(1, Math.round(words / 180));
                    const badge = document.createElement('div');
                    badge.id = 'tilcayo-word-counter';
                    badge.innerText = '📖 ' + words + ' слов · ~' + minutes + ' мин';
                    document.body.appendChild(badge);
                })();
            """.trimIndent(),
            matchUrlRegex = "^https?://.*",
            isEnabled = true,
            isPreinstalled = true,
            downloads = 980L
        ),
        Plugin(
            id = "plugin_wallpaper_fireflies",
            name = "Светлячки на закате (Живые обои)",
            description = "Анимированный интерактивный фон: парящие светящиеся огоньки на холсте HTML5 Canvas.",
            author = "Тилкайо Art",
            version = "1.0.0",
            iconName = "auto_awesome",
            category = "Живые обои",
            pluginType = "WALLPAPER",
            htmlCode = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
<style>
  html, body {
    margin: 0; padding: 0; width: 100%; height: 100%;
    overflow: hidden; background: transparent;
  }
  canvas { display: block; width: 100%; height: 100%; }
</style>
</head>
<body>
<canvas id="c"></canvas>
<script>
  const canvas = document.getElementById('c');
  const ctx = canvas.getContext('2d');
  let w, h;
  function resize() {
    w = canvas.width = window.innerWidth;
    h = canvas.height = window.innerHeight;
  }
  window.addEventListener('resize', resize);
  resize();

  const particles = [];
  for (let i = 0; i < 45; i++) {
    particles.push({
      x: Math.random() * w,
      y: Math.random() * h,
      vx: (Math.random() - 0.5) * 0.8,
      vy: (Math.random() - 0.5) * 0.8,
      r: Math.random() * 3 + 2,
      hue: Math.random() > 0.5 ? 42 : 175,
      alpha: Math.random() * 0.7 + 0.3,
      pulse: Math.random() * Math.PI
    });
  }

  function render() {
    ctx.clearRect(0, 0, w, h);
    particles.forEach(p => {
      p.x += p.vx;
      p.y += p.vy;
      p.pulse += 0.03;
      if (p.x < 0) p.x = w;
      if (p.x > w) p.x = 0;
      if (p.y < 0) p.y = h;
      if (p.y > h) p.y = 0;

      const currentAlpha = Math.sin(p.pulse) * 0.35 + 0.55;
      const glow = ctx.createRadialGradient(p.x, p.y, 0, p.x, p.y, p.r * 4);
      glow.addColorStop(0, 'hsla(' + p.hue + ', 95%, 65%, ' + currentAlpha + ')');
      glow.addColorStop(1, 'hsla(' + p.hue + ', 95%, 65%, 0)');

      ctx.fillStyle = glow;
      ctx.beginPath();
      ctx.arc(p.x, p.y, p.r * 4, 0, Math.PI * 2);
      ctx.fill();

      ctx.fillStyle = '#ffffff';
      ctx.beginPath();
      ctx.arc(p.x, p.y, p.r * 0.6, 0, Math.PI * 2);
      ctx.fill();
    });
    requestAnimationFrame(render);
  }
  render();
</script>
</body>
</html>
            """.trimIndent(),
            matchUrlRegex = ".*",
            isEnabled = true,
            isPreinstalled = true,
            downloads = 3400L
        ),
        Plugin(
            id = "plugin_wallpaper_waves",
            name = "Неоновые волны (Живые обои)",
            description = "Гармонические синусоидальные волны золота и лазури высокой четкости.",
            author = "Тилкайо Art",
            version = "1.0.0",
            iconName = "palette",
            category = "Живые обои",
            pluginType = "WALLPAPER",
            htmlCode = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
<style>
  html, body { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; background: transparent; }
  canvas { display: block; width: 100%; height: 100%; }
</style>
</head>
<body>
<canvas id="c"></canvas>
<script>
  const canvas = document.getElementById('c');
  const ctx = canvas.getContext('2d');
  let w, h, t = 0;
  function resize() { w = canvas.width = window.innerWidth; h = canvas.height = window.innerHeight; }
  window.addEventListener('resize', resize);
  resize();

  function draw() {
    ctx.clearRect(0, 0, w, h);
    t += 0.015;
    for (let i = 0; i < 4; i++) {
      ctx.beginPath();
      ctx.lineWidth = 2.5;
      const grad = ctx.createLinearGradient(0, 0, w, h);
      grad.addColorStop(0, i % 2 === 0 ? 'rgba(229,138,43,0.45)' : 'rgba(0,180,216,0.45)');
      grad.addColorStop(1, i % 2 === 0 ? 'rgba(255,200,80,0.2)' : 'rgba(114,9,183,0.2)');
      ctx.strokeStyle = grad;
      for (let x = 0; x < w; x += 6) {
        const y = h * 0.6 + Math.sin(x * 0.005 + t + i * 1.2) * 55 + Math.cos(x * 0.009 - t) * 35;
        if (x === 0) ctx.moveTo(x, y); else ctx.lineTo(x, y);
      }
      ctx.stroke();
    }
    requestAnimationFrame(draw);
  }
  draw();
</script>
</body>
</html>
            """.trimIndent(),
            matchUrlRegex = ".*",
            isEnabled = true,
            isPreinstalled = true,
            downloads = 2150L
        )
    )
}

