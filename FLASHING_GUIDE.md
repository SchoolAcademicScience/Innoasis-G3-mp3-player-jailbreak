# Innioasis G3 Custom ROM - Flashing Guide

## ⚠️ WARNING: READ BEFORE PROCEEDING

**This process can BRICK your device permanently.** Proceed at your own risk.
- Always backup your data first
- Ensure battery is >50% (preferably charging)
- Have SP Flash Tool and stock firmware ready for recovery
- This guide assumes Linux/WSL2 environment

---

## Prerequisites

### Hardware
- Innioasis G3 MP3 Player
- USB-C cable (data capable)
- Computer with Linux (native or WSL2) or Windows

### Software
- **SP Flash Tool** (Linux/Windows) - for MediaTek devices
- **Stock firmware** (`rom_g3.zip`) - from [Innioasis Support](https://www.innioasis.com/pages/support-details?type=g-3)
- **Custom recovery** (TWRP) - built from this project
- **Custom ROM zip** - built from this project
- **ADB/Fastboot** tools

### Downloads
```bash
# Stock firmware
wget "https://www.innioasis.com/firmware/G3/rom_g3.zip" -O rom_g3.zip

# SP Flash Tool (Linux)
wget "https://github.com/spflashtool/spflashtool/releases/latest/download/spflashtool-linux.zip"
unzip spflashtool-linux.zip
```

---

## Step 1: Backup Stock Firmware (CRITICAL)

Before ANY modification, backup your current partitions:

```bash
# Boot into stock recovery (Power + Volume Up)
# Connect via USB
adb shell

# In adb shell, backup critical partitions
dd if=/dev/block/by-name/boot of=/sdcard/boot.img
dd if=/dev/block/by-name/dtbo of=/sdcard/dtbo.img
dd if=/dev/block/by-name/vbmeta of=/sdcard/vbmeta.img
dd if=/dev/block/by-name/vbmeta_system of=/sdcard/vbmeta_system.img
dd if=/dev/block/by-name/vbmeta_vendor of=/sdcard/vbmeta_vendor.img
dd if=/dev/block/by-name/super of=/sdcard/super.img

# Pull to computer
adb pull /sdcard/boot.img
adb pull /sdcard/dtbo.img
adb pull /sdcard/vbmeta.img
adb pull /sdcard/vbmeta_system.img
adb pull /sdcard/vbmeta_vendor.img
adb pull /sdcard/super.img
```

**Store these backups safely!** They are your only way to recover from a brick.

---

## Step 2: Unlock Bootloader / Enable OEM Unlock

### Option A: Official Bootloader Unlock (if supported)
```bash
# Enable Developer Options
# Settings > About > Tap Build Number 7 times
# Settings > Developer Options > Enable OEM Unlocking
# Settings > Developer Options > Enable USB Debugging

# Reboot to fastboot
adb reboot bootloader

# Check if unlock is supported
fastboot oem unlock
# OR
fastboot flashing unlock
```

### Option B: SP Flash Tool (No Bootloader Unlock Required)
MediaTek devices can flash custom recovery via SP Flash Tool without unlocking bootloader.

1. Extract `rom_g3.zip` to get scatter file and images
2. Open SP Flash Tool
3. Load scatter file (`MT6753_Android_scatter.txt`)
4. **Only select recovery partition** for now
5. Flash `recovery.img` (TWRP)

---

## Step 3: Flash TWRP Recovery via SP Flash Tool

### Prepare SP Flash Tool
```bash
# On Linux
cd spflashtool
sudo ./flash_tool

# On Windows
# Run flash_tool.exe as Administrator
```

### Flash TWRP
1. Click **Scatter-loading** button
2. Select `MT6753_Android_scatter.txt` from extracted stock firmware
3. **Uncheck all partitions except `recovery`**
4. Click **Download** (green arrow)
5. Power off device completely
6. Hold **Volume Up + Volume Down** and connect USB
7. Wait for flashing to complete (green circle)
8. Disconnect and reboot to recovery: **Power + Volume Up**

### Verify TWRP
- Should see TWRP interface
- Swipe to allow modifications
- Go to **Mount** and enable **MTP** for file transfer

---

## Step 4: Flash Custom ROM

### Via TWRP (Recommended)
```bash
# Copy ROM zip to device
adb push out/target/product/g3/lineage-*.zip /sdcard/

# In TWRP:
# 1. Wipe > Advanced Wipe > Select: Dalvik/ART Cache, System, Data, Cache, Vendor
# 2. Swipe to Wipe
# 3. Install > Select lineage-*.zip > Swipe to Confirm Flash
# 4. (Optional) Install GApps zip if not included
# 5. Reboot > System
```

### Via SP Flash Tool (If TWRP fails)
```bash
# Flash all partitions from ROM build
# In SP Flash Tool, load scatter file
# Check: boot, dtbo, vbmeta, vbmeta_system, vbmeta_vendor, super
# Flash each corresponding .img file from out/target/product/g3/
```

---

## Step 5: Post-Install Setup

### First Boot
- Wait 5-10 minutes for first boot (optimizing apps)
- Complete setup wizard
- Enable Developer Options again
- Enable USB Debugging

### Verify Installation
```bash
adb shell getprop ro.build.display.id
# Should show: lineage_g3-userdebug 14 ...

adb shell getprop ro.lineage.version
# Should show: 21.x.x
```

---

## Step 6: Install Google Play Store (GApps)

If using pico/micro GApps (included in ROM):
- Play Store should work out of the box
- Sign in with Google account

If not included:
```bash
# Download appropriate GApps for ARM64, Android 14
# https://github.com/nikgapps/releases/tag/14-2024xxxx
# or https://sourceforge.net/projects/mindthegapps/files/arm64/14/

adb push MindTheGapps-*.zip /sdcard/
# Flash via TWRP > Install
```

---

## Recovery / Unbrick Procedures

### Soft Brick (Bootloop)
1. Boot to TWRP (Power + Volume Up)
2. Wipe Cache + Dalvik
3. Reflash ROM zip

### Hard Brick (No Boot, No Recovery)
Use SP Flash Tool with **stock firmware backup**:

```bash
# In SP Flash Tool
# Load scatter file
# Check ALL partitions
# Select stock .img files from your backup
# Click Download
# Connect device in download mode (Vol Up + Vol Down + USB)
```

### Emergency Download Mode (EDL)
If device won't enter download mode:
1. Short test points on PCB (requires disassembly)
2. Use EDL mode with Mi Flash / QFIL
3. Flash full stock firmware

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Device not detected in SP Flash Tool | Install MediaTek VCOM drivers; try different USB port/cable |
| "Download failed" error | Ensure correct scatter file; check partition sizes match |
| Bootloop after flash | Wipe data/cache in TWRP; re-flash ROM |
| Play Store crashes | Clear Play Store data; reinstall GApps; check Play Integrity |
| Low memory (128MB target) | Enable ZRAM in settings; reduce animation scales |
| Touchscreen not working | Flash correct dtbo.img; check kernel config |

---

## Memory Optimization Verification

After boot, verify memory optimizations:

```bash
# Check available memory
adb shell free -h

# Check ZRAM
adb shell cat /proc/swaps
adb shell cat /sys/block/zram0/comp_algorithm

# Check lowmemorykiller
adb shell cat /sys/module/lowmemorykiller/parameters/minfree

# Check running services
adb shell dumpsys activity services | head -50
```

Expected on 2GB device with optimizations:
- System: ~300MB (vs ~700MB stock)
- Free: ~1.2GB+ available for apps
- ZRAM: 128MB LZ4 compressed swap

---

## Reverting to Stock

```bash
# Via TWRP
# 1. Copy stock rom_g3.zip to device
# 2. Install > Install Image > Select all partitions from zip
# 3. Or use SP Flash Tool with stock scatter + images
```

---

## Support & Resources

- **Device Tree**: `device/innioasis/g3/`
- **Kernel Source**: `kernel/innioasis/mt6753/`
- **Vendor Blobs**: `vendor/innioasis/g3/`
- **Launcher**: `packages/apps/Launcher3QuickStep/`
- **Build Script**: `./build.sh all`

### Community
- XDA: [MT6753 Development](https://xdaforums.com/tags/mt6753/)
- Telegram: @MT6753Dev
- GitHub: Reference [Lumos-G1](https://github.com/Seph29/Lumos-G1) for MediaTek patching

---

## Legal Disclaimer

This is an **unofficial** custom ROM. Not affiliated with Innioasis.
- Voids manufacturer warranty
- May violate terms of service
- Use at your own risk
- No support from manufacturer

**By flashing this ROM, you accept full responsibility for any damage to your device.**