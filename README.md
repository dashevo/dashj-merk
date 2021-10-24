# Installing Build tools
```shell
rustup install nightly
```

# Build with Gradle
```shell
./gradlew assemble test
```

# Build with Gradle and Run Tests
```shell
./gradlew assemble test
```
# Build Rust Library Manually
```shell
cd src/main/rust
cargo +nightly build --release
```

# Publish
```shell
./gradlew uploadArchives
```

TODO: Include dashj_merk builds