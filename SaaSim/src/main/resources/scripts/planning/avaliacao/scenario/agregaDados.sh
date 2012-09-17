#!/bin/bash

#for heur in ut op on ov; do
for heur in ut op_partial_fac5 ; do

	for risco in 5 10 15; do
		rm -rf ${heur}_100_${risco}/profits.dat
		for index in `seq 1 70`; do
			head -n 2 ${heur}_100_${risco}/data_${index}.output | tail -n 1 >> ${heur}_100_${risco}/profits.dat
		done

	done
done

#for heur in ut op on ov; do
for heur in ut op_partial_fac5 ; do

	for risco in 5 10 15; do
		rm -rf ${heur}_50_${risco}/profits.dat
		for index in `seq 100 170`; do
			head -n 2 ${heur}_50_${risco}/data_${index}.output | tail -n 1 >> ${heur}_50_${risco}/profits.dat
		done

	done
done

#for heur in ut op on ov; do
for heur in ut op_partial_fac5 ; do

	for risco in 5 10 15; do
		rm -rf ${heur}_10_${risco}/profits.dat
		for index in `seq 200 270`; do
			head -n 2 ${heur}_10_${risco}/data_${index}.output | tail -n 1 >> ${heur}_10_${risco}/profits.dat
		done

	done
done
