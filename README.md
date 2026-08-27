# meta-pgo

## clang-native, llvm-native pgo

Profile-guided optimization for the clang/llvm **native** toolchain

```
local.conf

PACKAGECONFIG:append:pn-clang-native = " thin-lto-pgo"
PACKAGECONFIG:append:pn-llvm-native = " thin-lto-pgo"
```

```
clang-stage1-native      plain clang/lld, built with gcc
        |
clang-profdata-native    instrumented clang + training -> clang.profdata
        |
llvm-native              Release + PGO + ThinLTO
clang-native             Release + PGO + ThinLTO
```

## Results

Compiling sqlite3 for qemuarm64, `PREFERRED_TOOLCHAIN_TARGET = "clang"` do_compile buildstats time

| native clang configuration                  | sqlite3 do_compile |
|---------------------------------------------|--------------------|
| oe-core default CMAKE_BUILD_TYPE=MinSizeRel | 37.93s             |
| oe-core at CMAKE_BUILD_TYPE=Release         | 24.06s             |
| thin-lto-pgo                                | 18.29s             |
