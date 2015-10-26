# Handy Warup

## Handy what?

Handy Warup is a patch generator/applier written in Java.
The idea is to execute file operations, described in a specific
generated file, in batch.

It is obviously broken down into 2 parts:

 1. generator: simple (Linux-only) Bash script to generate a diff
file between two archives
 2. applier: Java program to consume the diff file and apply it to
a specified local directory

## Getting started

As simple as:

```shell
 $> mvn package
```
