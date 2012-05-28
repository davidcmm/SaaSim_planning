#!/bin/bash

for prime in `cat primes.txt`; do
	./geist_trg -c -r ${prime} peak
	mv peak.trc peak_${prime}.trc
done

for prime in `cat primes.txt`; do
	./geist_trg -c -r ${prime} typ
	mv typ.trc typ_${prime}.trc
done

for prime in `cat primes.txt`; do
	./geist_trg -c -r ${prime} under
	mv under.trc under_${prime}.trc
done
