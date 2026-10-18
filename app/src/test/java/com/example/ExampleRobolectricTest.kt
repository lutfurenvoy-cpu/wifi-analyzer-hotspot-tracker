package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.SpeedTestRecordEntity
import com.example.data.model.SpeedTestStage
import com.example.util.SpeedTestEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
    assertEquals("WiFiPulse", appName)
  }

  @Test
  fun `speed test engine initializes in idle state`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val engine = SpeedTestEngine(context)
    val state = engine.state.value
    assertEquals(SpeedTestStage.IDLE, state.stage)
    assertEquals(0.0, state.currentSpeedMbps, 0.01)
  }

  @Test
  fun `speed test database inserts and retrieves record`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getInstance(context)
    val record = SpeedTestRecordEntity(
      timestamp = System.currentTimeMillis(),
      downloadMbps = 85.5,
      uploadMbps = 32.1,
      pingMs = 15.0,
      jitterMs = 2.4,
      wifiSsid = "TestOfficeWiFi",
      serverName = "Cloudflare CDN Edge"
    )

    val id = db.speedTestRecordDao().insert(record)
    assertTrue(id > 0)

    val history = db.speedTestRecordDao().getAllHistory().first()
    assertTrue(history.isNotEmpty())
    val saved = history.find { it.wifiSsid == "TestOfficeWiFi" }
    assertNotNull(saved)
    assertEquals(85.5, saved!!.downloadMbps, 0.01)
    assertEquals(32.1, saved.uploadMbps, 0.01)
  }
}
