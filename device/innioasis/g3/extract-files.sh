#!/bin/bash
# Extract proprietary files from stock firmware for Innioasis G3
# Run this after extracting stock firmware to vendor/innioasis/g3/proprietary/

set -e

VENDOR_DIR=$(dirname "$0")/../../../vendor/innioasis/g3
PROPRIETARY_DIR="$VENDOR_DIR/proprietary"
EXTRACT_DIR="$VENDOR_DIR/extract"

mkdir -p "$PROPRIETARY_DIR"
mkdir -p "$EXTRACT_DIR"

echo "Extracting proprietary files for Innioasis G3..."

# List of proprietary files to extract from stock firmware
# These paths are relative to the root of the extracted firmware

PROPRIETARY_FILES=(
    # Audio
    "vendor/lib64/hw/audio.primary.mt6753.so"
    "vendor/lib64/hw/audio.r_submix.default.so"
    "vendor/lib64/hw/audio.usb.default.so"
    "vendor/lib64/libaudioutils.so"
    "vendor/lib64/libtinyalsa.so"
    "vendor/lib64/libmtkcodec.so"
    "vendor/lib64/libmtkplayer.so"
    "vendor/lib64/libstagefright_softomx_plugin.so"
    "vendor/lib64/libstagefright_softomx_venc_plugin.so"

    # Bluetooth
    "vendor/lib64/libbluetooth_mtk.so"
    "vendor/lib64/libbluetooth_mtk_pure.so"
    "vendor/lib64/libbt-vendor.so"
    "vendor/lib64/libbt-em.so"

    # Camera
    "vendor/lib64/hw/camera.mt6753.so"
    "vendor/lib64/libcamera_metadata.so"
    "vendor/lib64/libcameracustom.so"
    "vendor/lib64/libcam_utils.so"
    "vendor/lib64/libcamalgo.so"
    "vendor/lib64/libcamdrv.so"
    "vendor/lib64/libcamhal.so"
    "vendor/lib64/libcamhal_mtk.so"
    "vendor/lib64/libcamparamsmgr.so"
    "vendor/lib64/libcamscenario.so"
    "vendor/lib64/libcapturenrml.so"
    "vendor/lib64/libfeatureio.so"
    "vendor/lib64/libhdr.so"
    "vendor/lib64/libmhal.so"
    "vendor/lib64/libmhalImageCodec.so"
    "vendor/lib64/libmtkcam_stdutils.so"
    "vendor/lib64/libmtkcam_fwkutils.so"
    "vendor/lib64/libmtkcam_modulehelper.so"
    "vendor/lib64/libmtkcam_imgbuf.so"
    "vendor/lib64/libmtkcam_ulog.so"
    "vendor/lib64/libmtkcam_utils.so"
    "vendor/lib64/libnvram.so"
    "vendor/lib64/libnvram_daemon_callback.so"
    "vendor/lib64/libnvram_platform.so"
    "vendor/lib64/libnvram_sec.so"

    # Display
    "vendor/lib64/hw/gralloc.mt6753.so"
    "vendor/lib64/hw/hwcomposer.mt6753.so"
    "vendor/lib64/hw/memtrack.mt6753.so"
    "vendor/lib64/libdisplayconfig.so"
    "vendor/lib64/libhwc2on1adapter.so"
    "vendor/lib64/libhwc2onfbadapter.so"
    "vendor/lib64/libion.so"
    "vendor/lib64/libion_mtk.so"
    "vendor/lib64/libmtk_disp.so"
    "vendor/lib64/libmtk_disp_mgr.so"
    "vendor/lib64/libmtk_disp_ovl.so"
    "vendor/lib64/libmtk_disp_rdma.so"
    "vendor/lib64/libmtk_disp_wdma.so"
    "vendor/lib64/libmtk_ion.so"
    "vendor/lib64/libmtk_shifter.so"
    "vendor/lib64/libvcodec_oal.so"
    "vendor/lib64/libvcodecdrv.so"
    "vendor/lib64/libvp9dec_sa.ca7.so"
    "vendor/lib64/libvp9dec_sa.ca53.so"

    # DRM
    "vendor/lib64/mediadrm/libdrmclearkeyplugin.so"
    "vendor/lib64/mediadrm/libwvdrmengine.so"
    "vendor/lib64/libdrmclearkey.so"
    "vendor/lib64/libdrmframework.so"
    "vendor/lib64/libdrmframework_common.so"
    "vendor/lib64/libdrmmtkutil.so"
    "vendor/lib64/libdrmmtkwhitelist.so"
    "vendor/lib64/liboemcrypto.so"

    # GPS/GNSS
    "vendor/lib64/hw/gps.mt6753.so"
    "vendor/lib64/libcurl.so"
    "vendor/lib64/libgps.so"
    "vendor/lib64/libmnl.so"
    "vendor/lib64/libmtkgps.so"
    "vendor/lib64/libmtklbs.so"

    # Media
    "vendor/lib64/libmtk_vt_client.so"
    "vendor/lib64/libmtk_vt_service.so"
    "vendor/lib64/libmtk_vt_swip.so"
    "vendor/lib64/libmtk_vt_utils.so"
    "vendor/lib64/libmtk_vcodec.so"
    "vendor/lib64/libmtk_vcodec_v2.so"
    "vendor/lib64/libmtk_venc.so"
    "vendor/lib64/libmtk_vdec.so"

    # Radio/RIL
    "vendor/lib64/libmtk-ril.so"
    "vendor/lib64/libmtk-rilmd2.so"
    "vendor/lib64/librilmtk.so"
    "vendor/lib64/librilmtkmd2.so"
    "vendor/lib64/libratconfig.so"
    "vendor/lib64/libratutil.so"
    "vendor/lib64/libccci_util.so"
    "vendor/lib64/libmdloggerrecycle.so"

    # Sensors
    "vendor/lib64/hw/sensors.mt6753.so"
    "vendor/lib64/libsensorhub.so"
    "vendor/lib64/libsensorndkbridge.so"
    "vendor/lib64/libsensorlist.so"
    "vendor/lib64/libsensor_mgr.so"
    "vendor/lib64/libsensor_recorder.so"
    "vendor/lib64/libsensor_dynamic.so"

    # Thermal
    "vendor/lib64/hw/thermal.mt6753.so"
    "vendor/lib64/libthermal_mtk.so"

    # WiFi
    "vendor/lib64/hw/wifi.mt6753.so"
    "vendor/lib64/libwifi-hal-mediatek.so"
    "vendor/lib64/libwpa_client.so"
    "vendor/lib64/libhostapd.so"
    "vendor/bin/hostapd"
    "vendor/bin/wpa_supplicant"
    "vendor/etc/wifi/WIFI_RAM_CODE_MT6753_1.bin"
    "vendor/etc/wifi/WIFI_RAM_CODE_MT6753_2.bin"
    "vendor/etc/wifi/WIFI_MT6753_E1.bin"
    "vendor/etc/wifi/WIFI_MT6753_E2.bin"
    "vendor/etc/wifi/WIFI_MT6753_E3.bin"

    # Power
    "vendor/lib64/hw/power.mt6753.so"
    "vendor/lib64/libpowerhal.so"
    "vendor/lib64/libpowerhal_mtk.so"

    # Vibrator
    "vendor/lib64/hw/vibrator.mt6753.so"

    # Light
    "vendor/lib64/hw/lights.mt6753.so"

    # Gatekeeper/Keymaster
    "vendor/lib64/hw/gatekeeper.mt6753.so"
    "vendor/lib64/hw/keymaster.mt6753.so"
    "vendor/lib64/libkeymaster4.so"
    "vendor/lib64/libkeymaster4support.so"
    "vendor/lib64/libpuresoftkeymasterdevice.so"
    "vendor/lib64/libsoft_attestation_cert.so"
    "vendor/lib64/libgatekeeper.so"

    # Firmware
    "vendor/firmware/mt6753_e1_wifi.bin"
    "vendor/firmware/mt6753_e2_wifi.bin"
    "vendor/firmware/mt6753_e3_wifi.bin"
    "vendor/firmware/BT_RAM_CODE_MT6753_1.bin"
    "vendor/firmware/BT_RAM_CODE_MT6753_2.bin"
    "vendor/firmware/BT_RAM_CODE_MT6753_3.bin"
    "vendor/firmware/emi_mpu.bin"
    "vendor/firmware/mt6753_pmic_drv.bin"

    # Config files
    "vendor/etc/audio_param/VolumeGainMap_Amp.xml"
    "vendor/etc/audio_param/AudioParam.xml"
    "vendor/etc/audio_param/VolumeGainMap.xml"
    "vendor/etc/audio_policy_configuration.xml"
    "vendor/etc/audio_policy_volumes.xml"
    "vendor/etc/mtkcam/module.cfg"
    "vendor/etc/mtkcam/mtkcam.ini"
    "vendor/etc/mtklog-config.prop"
    "vendor/etc/thermal.conf"
    "vendor/etc/thermal.lic"
    "vendor/etc/wifi/wpa_supplicant.conf"
    "vendor/etc/wifi/wpa_supplicant_overlay.conf"
    "vendor/etc/wifi/p2p_supplicant_overlay.conf"
)

# Function to extract a file
extract_file() {
    local file=$1
    local src="$EXTRACT_DIR/$file"
    local dst="$PROPRIETARY_DIR/$file"

    if [ -f "$src" ]; then
        mkdir -p "$(dirname "$dst")"
        cp "$src" "$dst"
        echo "  Extracted: $file"
    else
        echo "  Missing: $file"
    fi
}

# Extract all files
for file in "${PROPRIETARY_FILES[@]}"; do
    extract_file "$file"
done

# Generate proprietary-files.txt
echo "Generating proprietary-files.txt..."
find "$PROPRIETARY_DIR" -type f | sed "s|$PROPRIETARY_DIR/||" | sort > "$VENDOR_DIR/proprietary-files.txt"

# Generate BoardConfigVendor.mk
echo "Generating BoardConfigVendor.mk..."
cat > "$VENDOR_DIR/BoardConfigVendor.mk" <<EOF
# Vendor BoardConfig for Innioasis G3
# Generated by extract-files.sh

BOARD_VENDORIMAGE_FILE_SYSTEM_TYPE := ext4
TARGET_COPY_OUT_VENDOR := vendor

# Vendor SPL
VENDOR_SECURITY_PATCH := 2026-08-05

# Properties
TARGET_VENDOR_PROP += $VENDOR_DIR/vendor.prop

# Sepolicy
BOARD_VENDOR_SEPOLICY_DIRS += $VENDOR_DIR/sepolicy

# VNDK
BOARD_VNDK_VERSION := current

# HIDL
DEVICE_MANIFEST_FILE += $VENDOR_DIR/manifest.xml
DEVICE_MATRIX_FILE += $VENDOR_DIR/compatibility_matrix.xml
EOF

echo "Done! Proprietary files extracted to $PROPRIETARY_DIR"
echo "File list: $VENDOR_DIR/proprietary-files.txt"