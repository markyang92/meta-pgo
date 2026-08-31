# meta-pgo

Profile-guided optimization for Yocto.

## Toolchain

The toolchain recipes are a compromise between peak PGO gains and
something that operates sanely inside a Yocto build:

- instrumented PGO only
- a dedicated producer recipe builds the instrumented tools and
  generates the profile data
- the original recipes receive that data through the sysroot and only
  consume it
- modifications to the original recipes are kept to a minimum

All measurements below are `do_compile` buildstats times on an
AMD Ryzen 9 7950X (16C/32T, `PARALLEL_MAKE = "-j 32"`), target
qemuarm64.

### clang-native, llvm-native

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

#### Results

Compiled with `PREFERRED_TOOLCHAIN_TARGET = "clang"`:

| native clang configuration                     | sqlite3 do_compile |
|------------------------------------------------|--------------------|
| oe-core default CMAKE_BUILD_TYPE=MinSizeRel    | 37.93s             |
| oe-core with CMAKE_BUILD_TYPE=Release          | 24.06s             |
| thin-lto-pgo with CMAKE_BUILD_TYPE=MinSizeRel  | 18.77s             |
| thin-lto-pgo with CMAKE_BUILD_TYPE=Release     | 18.29s             |
