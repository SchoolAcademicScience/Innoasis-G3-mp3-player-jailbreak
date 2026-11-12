#!/bin/bash
# Build script for Innioasis G3 Custom ROM
# Optimized for 128MB RAM target with modern UI

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
DEVICE="g3"
VENDOR="innioasis"
ROM_NAME="LineageOS"
ROM_VERSION="21"
ANDROID_VERSION="14"
BUILD_TYPE="userdebug"

# Paths
WORK_DIR=$(pwd)
DEVICE_DIR="$WORK_DIR/device/$VENDOR/$DEVICE"
KERNEL_DIR="$WORK_DIR/kernel/$VENDOR/mt6753"
VENDOR_DIR="$WORK_DIR/vendor/$VENDOR/$DEVICE"
OUT_DIR="$WORK_DIR/out/target/product/$DEVICE"

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_dependencies() {
    log_info "Checking build dependencies..."

    local deps=("repo" "git" "python3" "openjdk-17-jdk" "bc" "bison" "build-essential" "ccache" "curl" "flex" "g++-multilib" "gcc-multilib" "git" "gnupg" "gperf" "imagemagick" "lib32ncurses5-dev" "lib32readline-dev" "lib32z1-dev" "libelf-dev" "liblz4-tool" "libncurses5-dev" "libsdl1.2-dev" "libssl-dev" "libwxgtk3.0-gtk3-dev" "libxml2-utils" "lzop" "pngcrush" "rsync" "schedtool" "squashfs-tools" "xsltproc" "zip" "zlib1g-dev")

    for dep in "${deps[@]}"; do
        if ! dpkg -l | grep -q "^ii  $dep "; then
            log_warning "Missing dependency: $dep"
        fi
    done

    # Check for repo tool
    if ! command -v repo &> /dev/null; then
        log_warning "repo tool not found. Installing..."
        mkdir -p ~/bin
        curl https://storage.googleapis.com/git-repo-downloads/repo > ~/bin/repo
        chmod a+x ~/bin/repo
        export PATH=~/bin:$PATH
    fi

    log_success "Dependencies check complete"
}

setup_ccache() {
    log_info "Setting up ccache..."
    export USE_CCACHE=1
    export CCACHE_DIR="$WORK_DIR/.ccache"
    export CCACHE_EXEC=$(which ccache)
    ccache -M 50G
    ccache -z
    log_success "ccache configured (50GB)"
}

setup_build_env() {
    log_info "Setting up build environment..."

    # Set up Java
    export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
    export PATH=$JAVA_HOME/bin:$PATH

    # Set up build variables
    export ALLOW_MISSING_DEPENDENCIES=true
    export BUILD_BROKEN_DUP_RULES=true
    export BUILD_BROKEN_ELF_PREBUILT_PRODUCT_COPY_FILES=true
    export BUILD_BROKEN_ENFORCE_SYSPROP_OWNER=true
    export BUILD_BROKEN_MISSING_REQUIRED_MODULES=true
    export BUILD_BROKEN_PREBUILT_ELF_FILES=true
    export BUILD_BROKEN_USES_NETWORK=true
    export BUILD_BROKEN_VENDOR_PROPERTY_NAMESPACE=true
    export RELAX_USES_LIBRARY_CHECK=true
    export TEMPORARY_DISABLE_PATH_RESTRICTIONS=true

    # Memory optimization flags
    export JACK_SERVER_VM_ARGUMENTS="-Dfile.encoding=UTF-8 -XX:+TieredCompilation -Xmx4g"
    export ANDROID_JACK_VM_ARGS="-Dfile.encoding=UTF-8 -XX:+TieredCompilation -Xmx4g"

    log_success "Build environment configured"
}

sync_sources() {
    log_info "Syncing AOSP/LineageOS sources..."

    # Initialize repo if needed
    if [ ! -d ".repo" ]; then
        repo init -u https://github.com/LineageOS/android.git -b lineage-$ROM_VERSION --depth=1
    fi

    # Create local manifest for device
    mkdir -p .repo/local_manifests
    cat > .repo/local_manifests/g3.xml <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<manifest>
    <!-- Device tree -->
    <project path="device/$VENDOR/$DEVICE" name="android_device_$VENDOR_$DEVICE" remote="github" revision="lineage-$ROM_VERSION" />
    <project path="device/mediatek/mt6753-common" name="android_device_mediatek_mt6753-common" remote="github" revision="lineage-$ROM_VERSION" />

    <!-- Kernel -->
    <project path="kernel/$VENDOR/mt6753" name="android_kernel_$VENDOR_mt6753" remote="github" revision="lineage-$ROM_VERSION" />

    <!-- Vendor blobs -->
    <project path="vendor/$VENDOR/$DEVICE" name="android_vendor_$VENDOR_$DEVICE" remote="github" revision="lineage-$ROM_VERSION" />

    <!-- Hardware mediatek -->
    <project path="hardware/mediatek" name="android_hardware_mediatek" remote="github" revision="lineage-$ROM_VERSION" />

    <!-- Custom launcher -->
    <project path="packages/apps/Launcher3QuickStep" name="android_packages_apps_Launcher3QuickStep" remote="local" revision="main" />
</manifest>
EOF

    # Sync with optimized settings
    repo sync -j$(nproc) -c --no-clone-bundle --no-tags --optimized-fetch --prune --force-sync

    log_success "Sources synced"
}

extract_vendor_blobs() {
    log_info "Extracting vendor blobs from stock firmware..."

    if [ ! -f "$WORK_DIR/rom_g3.zip" ]; then
        log_error "Stock firmware rom_g3.zip not found in $WORK_DIR"
        log_info "Download from: https://www.innioasis.com/pages/support-details?type=g-3"
        return 1
    fi

    # Create extract directory
    mkdir -p "$WORK_DIR/extract"
    cd "$WORK_DIR/extract"

    # Extract payload.bin if present
    if [ -f "payload.bin" ]; then
        log_info "Extracting payload.bin..."
        python3 "$WORK_DIR/scripts/extract_payload.py" payload.bin .
    fi

    # Extract using payload_dumper if available
    if command -v payload_dumper &> /dev/null; then
        payload_dumper -o output payload.bin
    fi

    # Copy blobs to vendor directory
    mkdir -p "$VENDOR_DIR/proprietary"

    # List of partitions to extract
    local partitions=("boot" "dtbo" "vbmeta" "vbmeta_system" "vbmeta_vendor" "super")

    for part in "${partitions[@]}"; do
        if [ -f "output/$part.img" ]; then
            log_info "Extracting $part..."
            # Mount and copy files
            mkdir -p "mnt_$part"
            sudo mount -o ro "output/$part.img" "mnt_$part" 2>/dev/null || true
            if [ -d "mnt_$part" ]; then
                cp -r "mnt_$part"/* "$VENDOR_DIR/proprietary/" 2>/dev/null || true
                sudo umount "mnt_$part" 2>/dev/null || true
            fi
        fi
    done

    # Generate proprietary-files.txt
    cd "$VENDOR_DIR"
    find proprietary -type f | sort > proprietary-files.txt

    # Run extract script
    if [ -f "$DEVICE_DIR/extract-files.sh" ]; then
        bash "$DEVICE_DIR/extract-files.sh"
    fi

    log_success "Vendor blobs extracted"
}

build_kernel() {
    log_info "Building kernel..."

    cd "$KERNEL_DIR"

    # Set up toolchain
    export ARCH=arm64
    export SUBARCH=arm64
    export CROSS_COMPILE=aarch64-linux-android-
    export CROSS_COMPILE_ARM32=arm-linux-androideabi-
    export CLANG_TRIPLE=aarch64-linux-gnu-
    export CLANG_TRIPLE_ARM32=arm-linux-gnueabi-

    # Use Clang
    export CC=clang
    export CLANG=clang
    export CLANG_VERSION=16

    # Build
    make O=out g3_defconfig
    make O=out -j$(nproc) Image.gz-dtb

    # Copy to out directory
    mkdir -p "$OUT_DIR"
    cp out/arch/arm64/boot/Image.gz-dtb "$OUT_DIR/kernel"

    log_success "Kernel built"
}

build_rom() {
    log_info "Building ROM..."

    cd "$WORK_DIR"

    # Source build environment
    source build/envsetup.sh

    # Choose target
    lunch lineage_${DEVICE}-${BUILD_TYPE}

    # Build with optimizations
    make -j$(nproc) bacon 2>&1 | tee build.log

    # Check result
    if [ -f "$OUT_DIR/lineage-*.zip" ]; then
        log_success "ROM built successfully!"
        ls -lh "$OUT_DIR/lineage-*.zip"
    else
        log_error "Build failed. Check build.log"
        return 1
    fi
}

build_recovery() {
    log_info "Building TWRP recovery..."

    cd "$WORK_DIR"
    source build/envsetup.sh
    lunch lineage_${DEVICE}-${BUILD_TYPE}

    make -j$(nproc) recoveryimage

    if [ -f "$OUT_DIR/recovery.img" ]; then
        log_success "Recovery built: $OUT_DIR/recovery.img"
    else
        log_error "Recovery build failed"
        return 1
    fi
}

create_ota_package() {
    log_info "Creating OTA package..."

    cd "$WORK_DIR"
    source build/envsetup.sh
    lunch lineage_${DEVICE}-${BUILD_TYPE}

    make -j$(nproc) otapackage

    if [ -f "$OUT_DIR/lineage-*-ota-*.zip" ]; then
        log_success "OTA package created"
        ls -lh "$OUT_DIR/lineage-*-ota-*.zip"
    fi
}

clean_build() {
    log_info "Cleaning build..."
    cd "$WORK_DIR"
    make clean
    make clobber
    rm -rf "$OUT_DIR"
    ccache -C
    log_success "Build cleaned"
}

# Main
main() {
    case "${1:-build}" in
        deps)
            check_dependencies
            ;;
        sync)
            sync_sources
            ;;
        extract)
            extract_vendor_blobs
            ;;
        kernel)
            build_kernel
            ;;
        recovery)
            build_recovery
            ;;
        rom)
            build_rom
            ;;
        ota)
            create_ota_package
            ;;
        clean)
            clean_build
            ;;
        all)
            check_dependencies
            setup_ccache
            setup_build_env
            sync_sources
            extract_vendor_blobs
            build_kernel
            build_recovery
            build_rom
            create_ota_package
            ;;
        *)
            echo "Usage: $0 {deps|sync|extract|kernel|recovery|rom|ota|clean|all}"
            echo ""
            echo "Commands:"
            echo "  deps     - Check/install dependencies"
            echo "  sync     - Sync AOSP/LineageOS sources"
            echo "  extract  - Extract vendor blobs from stock firmware"
            echo "  kernel   - Build kernel"
            echo "  recovery - Build TWRP recovery"
            echo "  rom      - Build ROM"
            echo "  ota      - Create OTA package"
            echo "  clean    - Clean build"
            echo "  all      - Run full build pipeline"
            exit 1
            ;;
    esac
}

main "$@"