package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ThemeRepository
import com.example.model.SearchEngine
import com.example.model.ThemeMode
import com.example.model.WallpaperPresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Тилкайо браузер", appName)
  }

  @Test
  fun `verify theme repository default and toggle`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val themeRepo = ThemeRepository(context)

    // Initial theme should be LIGHT
    assertEquals(ThemeMode.LIGHT, themeRepo.wallpaperState.value.themeMode)

    // Toggle theme to DARK
    themeRepo.toggleThemeMode()
    assertEquals(ThemeMode.DARK, themeRepo.wallpaperState.value.themeMode)

    // Toggle theme back to LIGHT
    themeRepo.toggleThemeMode()
    assertEquals(ThemeMode.LIGHT, themeRepo.wallpaperState.value.themeMode)
  }

  @Test
  fun `verify light and dark wallpaper presets`() {
    val presets = WallpaperPresets.presets
    assertTrue(presets.any { it.isLight })
    assertTrue(presets.any { !it.isLight })

    val pearl = WallpaperPresets.getPreset("preset_pearl_white")
    assertNotNull(pearl)
    assertTrue(pearl.isLight)
  }

  @Test
  fun `verify search engines have icon drawables`() {
    assertEquals(R.drawable.ic_engine_google, SearchEngine.GOOGLE.iconRes)
    assertEquals(R.drawable.ic_engine_yandex, SearchEngine.YANDEX.iconRes)
  }

  @Test
  fun `verify animated wallpaper presets and repository selection`() {
    val animPresets = WallpaperPresets.animatedPresets
    assertTrue(animPresets.isNotEmpty())
    assertTrue(animPresets.any { it.id == "anim_aurora" })

    val context = ApplicationProvider.getApplicationContext<Context>()
    val themeRepo = ThemeRepository(context)

    themeRepo.selectAnimatedPreset("anim_aurora")
    assertTrue(themeRepo.wallpaperState.value.isAnimated)
    assertEquals("anim_aurora", themeRepo.wallpaperState.value.animatedPresetId)

    themeRepo.setWallpaperPlugin("user_plugin_custom")
    assertTrue(themeRepo.wallpaperState.value.isAnimated)
    assertEquals("user_plugin_custom", themeRepo.wallpaperState.value.activeWallpaperPluginId)
  }

  @Test
  fun `verify plugin manifest json serialization`() {
    val plugin = com.example.model.Plugin(
      id = "test_wp",
      name = "Живые светлячки",
      pluginType = "WALLPAPER",
      htmlCode = "<html><body><canvas></canvas></body></html>"
    )
    assertTrue(plugin.isWallpaperPlugin())
    val json = plugin.toJsonString()
    assertTrue(json.contains("test_wp"))

    val parsed = com.example.model.Plugin.fromJsonString(json)
    assertNotNull(parsed)
    assertEquals("test_wp", parsed?.id)
    assertEquals("WALLPAPER", parsed?.pluginType)
  }

  @Test
  fun `verify url and search formatting`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val browserRepo = com.example.data.BrowserRepository(context)

    assertEquals("https://ya.ru", browserRepo.formatSearchOrUrl("ya.ru"))
    assertEquals("https://google.com", browserRepo.formatSearchOrUrl("https://google.com"))
    assertTrue(browserRepo.formatSearchOrUrl("котики фото").contains("search"))
  }

  @Test
  fun `verify tts clean markdown for speech`() {
    val markdown = """
      # Заголовок Тилкайо
      Привет, **пользователь**! Вот *совет*:
      ```javascript
      console.log('test');
      ```
      > Важная цитата
      * Пункт один
      Подробнее: [Сайт](https://example.com)
    """.trimIndent()

    val cleaned = com.example.util.TilcayoTtsManager.cleanMarkdownForSpeech(markdown)
    assertTrue(cleaned.contains("Заголовок Тилкайо"))
    assertTrue(cleaned.contains("пользователь"))
    assertTrue(!cleaned.contains("**"))
    assertTrue(!cleaned.contains("```"))
    assertTrue(!cleaned.contains("console.log"))
  }

  @Test
  fun `verify markdown blocks parser`() {
    val input = """
      # Главный заголовок
      Текст параграфа
      ```kotlin
      val x = 10
      ```
      > Цитата котика
      * Пункт списка
    """.trimIndent()

    val blocks = com.example.ui.components.parseMarkdownBlocks(input)
    assertTrue(blocks.any { it is com.example.ui.components.MarkdownBlock.Heading && it.text == "Главный заголовок" })
    assertTrue(blocks.any { it is com.example.ui.components.MarkdownBlock.Code && it.language == "kotlin" })
    assertTrue(blocks.any { it is com.example.ui.components.MarkdownBlock.Blockquote })
    assertTrue(blocks.any { it is com.example.ui.components.MarkdownBlock.Bullet })
  }
}

