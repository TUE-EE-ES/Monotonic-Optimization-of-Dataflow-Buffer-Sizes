# Monotonic Optimization of Dataflow Buffer Sizes

## Introduction

This repository contains the implementation and the models related to the experiments in the paper:

> Hendriks, M., Ara, H. A., Geilen, M., Basten, T., Marin, R. G., de Jong, R., & van der Vlugt, S. (2019). Monotonic optimization of dataflow buffer sizes. Journal of Signal Processing Systems, 91(1), 21–32. <https://doi.org/10.1007/s11265-018-1415-2>

The author version of the paper can be found here:

<https://research.tue.nl/en/publications/monotonic-optimization-of-dataflow-buffer-sizes>

## Acknowledgement

This research was supported by the ARTEMIS joint undertaking under grant agreement no 621439 (ALMARVI).

## How to Use

Note that reproduction of results depends on the Windows executable `sdf3analyze-csdf.exe` in the repository. Reproduction of results on platforms that cannot run it, is possible by building the executable yourself from [SDF3](http://www.es.ele.tue.nl/sdf3), but we do not support it.

The experiments can be run by executing the following steps.

- Download and install Eclipse IDE for Java developers, e.g., <https://www.eclipse.org/downloads/packages/eclipse-ide-java-developers/neon3>. It should have the testing framework `JUnit` installed.
- Start Eclipse
- Right click the package explorer
- Select `New -> Java project`
- Create a project name
- Un-check `Use default location`
- Click `Browse` and select this the directory of this repository (the one that contains this `readme.md` file)
- Select `Finish`
- Open the `BenchmarkTest.java` file under `src/test/java/nl.esi.almarvi.benchmark/BenchmarkTest.java`
- Select one of the test methods in this file by double-clicking the method name (those with `@Test` above them)
- Press the `Run` button, or alternatively, right-click the selected method and select Run `As-> JUnit` test
- The output is written to the console view

Note that every row in the table in the article is represented by a test.
