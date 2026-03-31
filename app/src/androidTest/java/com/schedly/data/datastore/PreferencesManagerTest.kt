package com.schedly.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for PreferencesManager.
 * Tests DataStore preference operations for Ramadan offset and view mode.
 */
class PreferencesManagerTest {

    private lateinit var context: Context
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var preferencesManager: PreferencesManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        dataStore = context.dataStore
        preferencesManager = PreferencesManager(dataStore)
    }

    @After
    fun teardown() = runTest {
        // Clear all preferences after each test
        dataStore.updateData { it.toMutablePreferences().apply { clear() } }
    }

    @Test
    fun `getRamadanOffset returns default 0 when not set`() = runTest {
        val offset = preferencesManager.getRamadanOffset()

        assertEquals(0, offset)
    }

    @Test
    fun `setRamadanOffset stores value correctly`() = runTest {
        preferencesManager.setRamadanOffset(1)

        val offset = preferencesManager.getRamadanOffset()

        assertEquals(1, offset)
    }

    @Test
    fun `setRamadanOffset stores negative value correctly`() = runTest {
        preferencesManager.setRamadanOffset(-1)

        val offset = preferencesManager.getRamadanOffset()

        assertEquals(-1, offset)
    }

    @Test
    fun `getRamadanOffsetFlow emits updated value`() = runTest {
        val flow = preferencesManager.getRamadanOffsetFlow()

        // Initial value should be 0
        assertEquals(0, flow.first())

        // Update and verify flow emits new value
        preferencesManager.setRamadanOffset(1)
        assertEquals(1, flow.first())
    }

    @Test
    fun `setRamadanOffset overwrites previous value`() = runTest {
        preferencesManager.setRamadanOffset(1)
        preferencesManager.setRamadanOffset(-1)

        val offset = preferencesManager.getRamadanOffset()

        assertEquals(-1, offset)
    }

    @Test
    fun `getRamadanOffset returns last set value after multiple updates`() = runTest {
        preferencesManager.setRamadanOffset(1)
        preferencesManager.setRamadanOffset(0)
        preferencesManager.setRamadanOffset(-1)
        preferencesManager.setRamadanOffset(1)

        val offset = preferencesManager.getRamadanOffset()

        assertEquals(1, offset)
    }

    @Test
    fun `getLastViewMode returns default VERTICAL when not set`() = runTest {
        val mode = preferencesManager.getLastViewModeFlow().first()

        assertEquals("VERTICAL", mode)
    }

    @Test
    fun `setLastViewMode stores value correctly`() = runTest {
        preferencesManager.setLastViewMode("HORIZONTAL")

        val mode = preferencesManager.getLastViewModeFlow().first()

        assertEquals("HORIZONTAL", mode)
    }

    @Test
    fun `setLastViewMode overwrites previous value`() = runTest {
        preferencesManager.setLastViewMode("HORIZONTAL")
        preferencesManager.setLastViewMode("VERTICAL")

        val mode = preferencesManager.getLastViewModeFlow().first()

        assertEquals("VERTICAL", mode)
    }

    @Test
    fun `getLastViewModeFlow emits updated value`() = runTest {
        val flow = preferencesManager.getLastViewModeFlow()

        // Initial value should be VERTICAL
        assertEquals("VERTICAL", flow.first())

        // Update and verify flow emits new value
        preferencesManager.setLastViewMode("HORIZONTAL")
        assertEquals("HORIZONTAL", flow.first())
    }

    @Test
    fun `multiple preferences can be stored independently`() = runTest {
        preferencesManager.setRamadanOffset(1)
        preferencesManager.setLastViewMode("HORIZONTAL")

        val offset = preferencesManager.getRamadanOffset()
        val mode = preferencesManager.getLastViewModeFlow().first()

        assertEquals(1, offset)
        assertEquals("HORIZONTAL", mode)
    }

    @Test
    fun `Ramadan offset and view mode do not interfere with each other`() = runTest {
        // Set Ramadan offset
        preferencesManager.setRamadanOffset(-1)
        assertEquals(-1, preferencesManager.getRamadanOffset())
        assertEquals("VERTICAL", preferencesManager.getLastViewModeFlow().first())

        // Set view mode
        preferencesManager.setLastViewMode("HORIZONTAL")
        assertEquals(-1, preferencesManager.getRamadanOffset())
        assertEquals("HORIZONTAL", preferencesManager.getLastViewModeFlow().first())

        // Update Ramadan offset again
        preferencesManager.setRamadanOffset(1)
        assertEquals(1, preferencesManager.getRamadanOffset())
        assertEquals("HORIZONTAL", preferencesManager.getLastViewModeFlow().first())
    }
}
