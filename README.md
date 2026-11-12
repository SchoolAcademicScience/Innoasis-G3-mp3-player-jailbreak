# Innioasis G3 Custom ROM Project

> **A modern, optimized, lightweight custom ROM for the Innioasis G3 MP3 Player**
> Target: **Smooth performance on 128MB RAM budget** (device has 2GB, heavily optimized)

![Android](https://img.shields.io/badge/Android-14%20(LineageOS%2021)-green)
![Device](https://img.shields.io/badge/Device-Innioasis%20G3-blue)
![SoC](https://img.shields.io/badge/SoC-MediaTek%20MT6753-orange)
![RAM](https://img.shields.io/badge/RAM-2GB%20(optimized%20for%20128MB)-red)
![Status](https://img.shields.io/badge/Status-In%20Development-yellow)

---

## 📱 Device Specifications

| Component | Specification |
|-----------|---------------|
| **Model** | Innioasis G3 MP3 Player |
| **SoC** | MediaTek MT6753 (8× Cortex-A53 @ 1.5 GHz, 28nm) |
| **GPU** | ARM Mali-T720 MP3 @ 700 MHz |
| **RAM** | 2GB LPDDR3 |
| **Storage** | 32GB eMMC + microSD up to 1TB |
| **Display** | 4.0" Touchscreen, 720p/1080p video |
| **Battery** | 1500mAh, USB-C 2hr charge |
| **Connectivity** | BT 4.2, WiFi 2.4/5GHz |
| **Stock OS** | Android 9 (Pie) - firmware `G3-9_*` |

---

## 🎯 Project Goals

### Primary Objectives
- ✅ **Modern UI**: Material You design, smooth 60fps animations
- ✅ **Extreme Optimization**: Target 128MB RAM footprint (system <150MB)
- ✅ **Google Play Store**: Full GApps support with Play Integrity
- ✅ **APK Support**: Sideload any Android app
- ✅ **Media Focused**: Optimized audio/video playback

### Technical Targets
| Metric | Stock | Target |
|--------|-------|--------|
| System Memory | ~700MB | **<150MB** |
| Launcher Memory | ~150MB | **<20MB** |
| Play Services | ~300MB | **<50MB** (microG/pico) |
| Boot Time | ~60s | **<30s** |
| App Launch | Variable | **<500ms** cold |

---

## 🏗️ Architecture Overview

```
├── device/innioasis/g3/          # Device tree (BoardConfig, init, fstab)
├── kernel/innioasis/mt6753/      # Kernel source (g3_defconfig)
├── vendor/innioasis/g3/          # Proprietary blobs (extracted from stock)
├── packages/apps/Launcher3QuickStep/  # Custom lightweight launcher
├── build.sh                      # Automated build script
├── scripts/                      # Helper scripts (payload extractor)
├── DEVICE_SPECS.md               # Detailed hardware documentation
├── FLASHING_GUIDE.md             # Step-by-step installation guide
└── README.md                     # This file
```

---

## 🚀 Quick Start

### Prerequisites
- Linux (Ubuntu 22.04+ recommended) or WSL2
- 50GB+ free disk space
- 16GB+ RAM (swap OK)
- Good internet connection

### One-Command Build
```bash
# Clone/download this project
cd "Lukes mp3 player jailbreak"

# Make build script executable
chmod +x build.sh

# Run full build pipeline (takes 2-6 hours first time)
./build.sh all
```

### Individual Steps
```bash
./build.sh deps      # Check dependencies
./build.sh sync      # Sync LineageOS sources
./build.sh extract   # Extract vendor blobs from stock firmware
./build.sh kernel    # Build kernel
./build.sh recovery  # Build TWRP recovery
./build.sh rom       # Build ROM zip
./build.sh ota       # Create OTA package
./build.sh clean     # Clean build
```

---

## 📦 Key Components

### 1. Custom Kernel (`kernel/innioasis/mt6753/`)
- **Base**: Linux 5.10 LTS (MediaTek common)
- **Optimizations**:
  - ZRAM with LZ4 compression (128MB)
  - KSM (Kernel Samepage Merging)
  - Aggressive LMK tuning
  - BFQ I/O scheduler
  - CPU governor: schedutil
  - GPU governor: simple_ondemand

### 2. Lightweight Launcher (`packages/apps/Launcher3QuickStep/`)
- **Language**: Kotlin + XML (no Compose for memory)
- **Features**:
  - Paged workspace (4×5 grid)
  - Hotseat (5 favorite apps)
  - Dock (4 recent apps)
  - All Apps drawer with search
  - Material You dynamic colors
  - Gesture navigation
  - <20MB RAM usage

### 3. Memory Optimizations (System-wide)
```properties
# Dalvik/ART
dalvik.vm.heapstartsize=4m
dalvik.vm.heapgrowthlimit=64m
dalvik.vm.heapsize=128m
dalvik.vm.heaptargetutilization=0.75

# ZRAM
ro.zram.mark_idle_delay_mins=60
ro.zram.first_wb_delay_mins=1440

# LMK
ro.lmk.low=1001
ro.lmk.medium=800
ro.lmk.high=600
ro.lmk.critical=400

# App limits
ro.sys.fw.bg_apps_limit=16
ro.config.max_starting_bg=8
```

### 4. GApps Integration
- **Package**: MicroG / pico GApps (ARM64, Android 14)
- **Components**: Play Store, Play Services, GSF, Account Manager
- **Play Integrity**: Basic attestation via MicroG
- **Size**: ~50MB vs 300MB+ full GApps

---

## 🔧 Development

### Building from Source
```bash
# Setup environment
source build/envsetup.sh
lunch lineage_g3-userdebug

# Build specific module
mka Launcher3QuickStep

# Build kernel only
cd kernel/innioasis/mt6753
make O=out g3_defconfig
make O=out -j$(nproc) Image.gz-dtb
```

### Adding Features
1. **System Apps**: Add to `device/innioasis/g3/g3.mk`
2. **Kernel Modules**: Modify `g3_defconfig`
3. **Overlay Resources**: `device/innioasis/g3/overlay/`
4. **SEPolicy**: `device/innioasis/g3/sepolicy/`

### Debugging
```bash
# Logcat
adb logcat -b all

# Kernel logs
adb shell dmesg

# Memory info
adb shell cat /proc/meminfo
adb shell free -h

# Performance
adb shell dumpsys gfxinfo com.innioasis.launcher3
```

---

## 📋 Flashing Instructions

**See [FLASHING_GUIDE.md](FLASHING_GUIDE.md) for complete step-by-step guide.**

### Quick Summary
1. **Backup stock firmware** (CRITICAL)
2. **Flash TWRP** via SP Flash Tool
3. **Boot TWRP** (Power + Vol Up)
4. **Wipe** System, Data, Cache, Vendor
5. **Flash** `lineage-*.zip`
6. **Flash** GApps (if separate)
7. **Reboot**

---

## ⚠️ Known Issues / Limitations

| Issue | Status | Workaround |
|-------|--------|------------|
| Camera | ❌ Not tested | May need proprietary libs |
| FM Radio | ❌ Not implemented | Requires MediaTek HAL |
| Bluetooth Audio | ⚠️ Partial | A2DP works, calls untested |
| DRM (Widevine) | ❌ L3 only | No L1 without certified blobs |
| Play Integrity | ⚠️ Basic | MicroG provides basic attestation |
| OTA Updates | 🔄 In progress | Manual flash for now |

---

## 🤝 Credits & References

- **LineageOS Project** - Base ROM source
- **Lumos-G1** (Seph29) - MediaTek patching reference
- **MT6753 Common** (janengelmohr) - Device tree reference
- **MicroG Project** - GApps alternative
- **SP Flash Tool** - MediaTek flashing utility
- **TWRP Team** - Custom recovery
- **Innioasis** - Hardware manufacturer

### Key Resources
- [MT6753 LineageOS Port](https://xdaforums.com/t/lineage-14-1-mt6753-7-1-2-ulefone-power-03-02-2019-unofficial.3828495/)
- [Lumos-G1 GitHub](https://github.com/Seph29/Lumos-G1)
- [MediaTek MT6753 Specs](https://www.notebookcheck.net/Mediatek-MT6753-SoC-Benchmarks-and-Specs.272904.0.html)
- [Innioasis G3 Support](https://www.innioasis.com/pages/support-details?type=g-3)

---

## 📄 License

```
This project is for educational and research purposes only.
Not affiliated with Innioasis or MediaTek.

Custom ROM components:
- LineageOS: Apache 2.0 / GPL
- Kernel: GPL v2
- Launcher: Apache 2.0
- MicroG: Apache 2.0

Proprietary blobs: Copyright their respective owners.
Extracting/using blobs requires owning the device.
```

---

## 💬 Support

- **Issues**: GitHub Issues (this repo)
- **Discussions**: GitHub Discussions
- **Telegram**: @MT6753Dev (community)
- **XDA**: [MT6753 Development Thread](https://xdaforums.com/tags/mt6753/)

---

## 🗺️ Roadmap

- [ ] **v1.0** - Initial release (boot, launcher, basic apps)
- [ ] **v1.1** - GApps integration, Play Store working
- [ ] **v1.2** - Audio HAL, Bluetooth, WiFi stability
- [ ] **v1.3** - Camera support, FM Radio
- [ ] **v2.0** - Android 15 (LineageOS 22), Kernel 6.x
- [ ] **OTA** - Built-in OTA updater
- [ ] **Themes** - Material You theme engine

---

**Built with ❤️ for the Innioasis G3 community**

*Last updated: 2026-11-11*