# MicroG / pico GApps for Innioasis G3 (ARM64, Android 14)
# Minimal package for Play Store support

PRODUCT_PACKAGES += \
    # Core GMS
    GmsCore \
    GsfProxy \
    FakeStore \
    PlayStore \
    PlayServices \
    GoogleAccountManager \
    GoogleBackupTransport \
    GoogleContactsSyncAdapter \
    GoogleCalendarSyncAdapter \
    GoogleFeedback \
    GoogleLoginService \
    GoogleOneTimeInitializer \
    GooglePartnerSetup \
    GoogleServicesFramework \
    Phonesky \
    PrebuiltGmsCore \
    PrebuiltGmsCore_PICO \
    PrebuiltGmsCore_Pico \
    SetupWizard \
    Velvet \

    # MicroG components (if using MicroG)
    GmsCore_MicroG \
    GsfProxy_MicroG \
    FakeStore_MicroG \
    PlayServices_MicroG \
    UnifiedNlp \
    NominatimNlpBackend \
    OpenBmapNlpBackend \
    MozillaNlpBackend \
    DeezerNlpBackend \
    AppleNlpBackend \

    # Permissions
    privapp-permissions-gms.xml \
    privapp-permissions-microg.xml \

    # Overlay for GMS
    GmsCoreOverlay \
    PlayStoreOverlay \

# Copy permissions
PRODUCT_COPY_FILES += \
    vendor/gapps/arm64/permissions/privapp-permissions-gms.xml:$(TARGET_COPY_OUT_SYSTEM)/etc/permissions/privapp-permissions-gms.xml \
    vendor/gapps/arm64/permissions/privapp-permissions-microg.xml:$(TARGET_COPY_OUT_SYSTEM)/etc/permissions/privapp-permissions-microg.xml \

# GMS properties
PRODUCT_PROPERTY_OVERRIDES += \
    ro.com.google.clientidbase=android-innioasis \
    ro.com.google.gmsversion=14.0_r0 \
    ro.setupwizard.mode=OPTIONAL \
    ro.setupwizard.rotation_locked=true \
    ro.setupwizard.require_network=false \
    ro.setupwizard.gservices_delay=-1 \
    setupwizard.feature.skip_button_use_mobile_data.carrier1839=true \
    setupwizard.feature.show_pai_screen_in_main_flow.carrier1839=false \
    setupwizard.feature.show_pixel_tos=false \

# Disable some GMS features for low memory
PRODUCT_PROPERTY_OVERRIDES += \
    persist.sys.gms_core.enabled=false \
    persist.sys.gsf.enabled=false \
    ro.gms.override.disable=true \

# Play Integrity (basic attestation via MicroG)
PRODUCT_PROPERTY_OVERRIDES += \
    ro.microg.play_integrity.enabled=true \
    ro.microg.play_integrity.basic_attestation=true \