
#======================================================================
# Bare-bones Bazel BUILD file for MyPL
# CPSC 321
# Spring, 2022
#======================================================================

load("@rules_java//java:defs.bzl", "java_test")

# TODO: Can make src/*.java a "filegroup"

java_binary(
  name = "hw6",
  srcs = glob(["src/*.java"]),
  main_class = "HW6",
)

java_library(
  name = "mypl-lib",
  srcs = glob(["src/*.java"]),
)

#----------------------------------------------------------------------
# TEST SUITES:
#----------------------------------------------------------------------


java_test(
    name = "vm-test",
    srcs = ["tests/VMTest.java"], 
    test_class = "VMTest",
    deps = ["lib/junit-4.13.2.jar", "lib/hamcrest-core-1.3.jar", ":mypl-lib"],
)



