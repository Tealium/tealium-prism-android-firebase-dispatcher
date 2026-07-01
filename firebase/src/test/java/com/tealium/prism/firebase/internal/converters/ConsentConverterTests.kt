package com.tealium.prism.firebase.internal.converters

import com.google.firebase.analytics.FirebaseAnalytics.ConsentStatus
import com.google.firebase.analytics.FirebaseAnalytics.ConsentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConsentConverterTests {

    @Test
    fun typeOrNull_returns_enum_for_known_raw_values() {
        assertEquals(ConsentType.AD_STORAGE, ConsentConverter.typeOrNull("ad_storage"))
        assertEquals(ConsentType.ANALYTICS_STORAGE, ConsentConverter.typeOrNull("analytics_storage"))
        assertEquals(ConsentType.AD_USER_DATA, ConsentConverter.typeOrNull("ad_user_data"))
        assertEquals(ConsentType.AD_PERSONALIZATION, ConsentConverter.typeOrNull("ad_personalization"))
    }

    @Test
    fun typeOrNull_is_case_insensitive() {
        assertEquals(ConsentType.AD_STORAGE, ConsentConverter.typeOrNull("AD_STORAGE"))
        assertEquals(ConsentType.AD_STORAGE, ConsentConverter.typeOrNull("Ad_Storage"))
    }

    @Test
    fun typeOrNull_returns_null_for_unknown_raw_values() {
        assertNull(ConsentConverter.typeOrNull("future_consent_flag"))
    }

    @Test
    fun statusOrNull_returns_enum_for_known_raw_values() {
        assertEquals(ConsentStatus.GRANTED, ConsentConverter.statusOrNull("granted"))
        assertEquals(ConsentStatus.DENIED, ConsentConverter.statusOrNull("denied"))
    }

    @Test
    fun statusOrNull_is_case_insensitive() {
        assertEquals(ConsentStatus.GRANTED, ConsentConverter.statusOrNull("GRANTED"))
    }

    @Test
    fun statusOrNull_returns_null_for_unknown_raw_values() {
        assertNull(ConsentConverter.statusOrNull("maybe"))
    }

    @Test
    fun rawValue_returns_spec_string_for_known_types() {
        assertEquals("ad_storage", ConsentConverter.rawValue(ConsentType.AD_STORAGE))
        assertEquals("analytics_storage", ConsentConverter.rawValue(ConsentType.ANALYTICS_STORAGE))
        assertEquals("ad_user_data", ConsentConverter.rawValue(ConsentType.AD_USER_DATA))
        assertEquals("ad_personalization", ConsentConverter.rawValue(ConsentType.AD_PERSONALIZATION))
    }
}
