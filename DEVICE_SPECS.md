# Innioasis G3 - Hardware Specifications & Jailbreak Research

## Device Overview
- **Model**: Innioasis G3 MP3 Player
- **SoC**: MediaTek MT6753 (8x Cortex-A53 @ 1.3-1.5 GHz, 28nm)
- **GPU**: ARM Mali-T720 MP3 @ 700 MHz
- **RAM**: 2GB LPDDR3 (user target: optimize to run comfortably in 128MB)
- **Storage**: 32GB eMMC + microSD up to 1TB
- **Display**: 4.0" touchscreen, 720p/1080p video playback support
- **Battery**: 1500mAh Li-ion, USB-C charging (2hr full charge)
- **Connectivity**: Bluetooth 4.2 (dual-way), WiFi 2.4/5GHz dual-band
- **Audio**: MP3, WAV, FLAC, AAC, APE, OGG, M4A, WMA, MP2
- **Video**: 720p/1080p hardware decoding

## Software
- **Stock OS**: Android 9 (Pie) - firmware versions `G3-9_EN-V2.04-20260805`, `G3-EN-WM-V6.01-20260805`
- **Kernel**: Linux 4.x (MediaTek 4.4/4.9 common kernel)
- **Bootloader**: Locked (MediaTek standard)
- **Recovery**: Stock recovery only

## Bootloader Unlock Status
- **Official unlock**: Not supported by manufacturer
- **MediaTek SP Flash Tool**: Can flash custom recovery/boot images without bootloader unlock
- **Precedent**: Lumos-G1 project shows G1 (similar device) can patch system.img/boot.img without bootloader unlock using `mtk-su` and `xdelta3`
- **Risk**: Bricking possible - recovery requires SP Flash Tool with stock firmware

## Custom Development Path

### Option A: SP Flash Tool + Custom Recovery (Recommended)
1. Extract stock firmware (boot.img, recovery.img, system.img, dtbo.img, vbmeta.img)
2. Port TWRP recovery for MT6753
3. Flash TWRP via SP Flash Tool
4. Build/install custom ROM from TWRP

### Option B: System Partition Patching (Lumos-G1 style)
1. Use temporary root (mtk-su) to patch boot.img + system.img
2. Install custom packages without full ROM replacement
3. Lower risk but limited customization

### Option C: Bootloader Unlock (If possible)
- Some MediaTek devices allow unlock via `fastboot oem unlock` or manufacturer codes
- Need to test in fastboot mode

## Memory Optimization Targets
| Component | Stock | Target (128MB budget) |
|-----------|-------|----------------------|
| System UI | ~200MB | <30MB |
| Launcher | ~150MB | <20MB |
| Play Services | ~300MB | <50MB (microG/pico GApps) |
| Kernel | ~50MB | ~50MB |
| **Total** | **~700MB+** | **~150MB** |

## Key Resources
- **MT6753 LineageOS ports**: [XDA thread](https://xdaforums.com/t/lineage-14-1-mt6753-7-1-2-ulefone-power-03-02-2019-unofficial.3828495/), [GitHub common](https://github.com/janengelmohr/android_device_mt6753_common)
- **Lumos-G1 reference**: [GitHub](https://github.com/Seph29/Lumos-G1) - patches system without bootloader unlock
- **SP Flash Tool**: MediaTek official flashing tool
- **TWRP porting guide**: [MT6753 TWRP port](https://www.getdroidtips.com/port-twrp-mediatek-64-bit-mt67xx-devices/)

## Next Steps
1. Obtain stock firmware (rom_g3.zip from Innioasis support)
2. Extract partition images using payload_dumper or SP Flash Tool scatter file
3. Analyze partition layout (super/partition structure)
4. Set up build environment (AOSP/LineageOS 21+ for Android 14/15)
5. Create device tree for G3
6. Port kernel from stock sources
7. Build minimal GApps package