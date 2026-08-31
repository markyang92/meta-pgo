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

### gcc-cross

Profile-guided optimization (with LTO) for the cross compiler itself.
gcc's own `profiledbootstrap` is native-only — bootstrap presumes the
compiler's output runs on the build machine, and no distro ships a
PGO-built cross gcc — so the producer recreates the training step by
hand: build the host tools instrumented, compile a training workload
with the instrumented compiler, and hand the resulting `.gcda` tree to the
real gcc-cross through the sysroot.

The awkward part is instrumenting a compiler that targets the target:
in the Yocto build flow gcc-cross is the bottom rung of the bootstrap
— glibc does not exist yet when it builds — so at that point there is
no target sysroot the instrumented compiler could compile anything
against. The stage1 shadow ladder fills exactly that gap: a minimal
parallel compiler-and-libc chain that assembles a training sysroot
with no dependency path back to the real gcc-cross (which now depends
on the producer, so a real-chain path would be a cycle).

sqlite3's amalgamation is an unusually convenient training workload
for this:

- one huge real-world C translation unit that exercises the compiler
  end to end
- needs nothing beyond libc headers
- public domain, so the file can simply be copied into the layer
  (`SRC_URI` cannot deliver it — the recipe shares gcc's work-shared
  source tree, which disables fetch/unpack)
- does not care which gcc version it trains

```
local.conf

GCC_CROSS_LTO_PGO = "1"
```

```
gcc-cross-stage1 -> libgcc-stage1-initial -> glibc-stage1
                         training sysroot, with no dependency
                         path back to the real gcc-cross
        |
gcc-cross-profdata       instrumented host tools + sqlite3.c training -> .gcda tree
        |
gcc-cross                rebuilt with -fprofile-use -flto=auto
```

The training workload is C only; C++ compiles still inherit most of
the benefit through the shared middle-end and backend objects.

#### Results

| gcc-cross configuration      | sqlite3 do_compile | linux-yocto 6.18 do_compile |
|------------------------------|--------------------|-----------------------------|
| oe-core default              | 32.5s              | 66.2s                       |
| GCC_CROSS_LTO_PGO (PGO+LTO)  | 28.8s              | 61.4s                       |

LTO alone gains almost nothing on the compiler; it pays off only in
combination with the profile.

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
