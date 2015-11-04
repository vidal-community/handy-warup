# Handy Warup

![Handy Warup](assets/logo_small.png)

[![Build Status](https://travis-ci.org/vidal-community/handy-warup.svg?branch=master)](https://travis-ci.org/vidal-community/handy-warup)
[![Coverage Status](https://coveralls.io/repos/vidal-community/handy-warup/badge.svg?branch=master&service=github)](https://coveralls.io/github/vidal-community/handy-warup?branch=master)


## Handy what?

> Diff once, apply everywhere.
>
> -- <cite>Guybrush Threepwood</cite>

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
 $> mvn install
```
