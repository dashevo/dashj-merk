# Installing Build tools
```shell
rustup install nightly-2021-03-25
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
cargo +nightly-2021-03-25 build --release
```

# Publish
```shell
./gradlew uploadArchives
```

TODO: Include dashj_merk builds