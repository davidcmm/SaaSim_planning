#!/bin/bash
#This script executes two phases of simulation: i) the capacity planning process; ii) workload execution.

if [ $# -lt 2 ]; then
        echo "usage: $0 <scenarios dir> <planning heuristic: Optimal, Over, History or On-demand>"
        exit 1
fi

find_self() {
        SELF=`dirname $0`
}

set_classpath() {
        LIB_DIR="$SELF/lib"
        CLASSPATH="$SELF/bin"
        for lib in $LIB_DIR/*; do
                CLASSPATH="$CLASSPATH:$lib"
        done
}

find_self && set_classpath

dir=$1
plan_heur=$2

mkdir result_${plan_heur}

for value in `seq 1 70`; do #Scenarios to be simulated. Here each year of workload for a number of X SaaS clients was stored in a folder named scenario_${value}

	cp ${dir}/scenario_${value}/users.properties .
	cp ${dir}/scenario_${value}/*.trc .
	rm .je.dat
	
	if [ ${plan_heur} = "Optimal" ] ; then
		java -Xmx2024m -server -cp $CLASSPATH commons.util.AggregateWorkload david.properties
		cp david.properties ${plan_heur}.properties
		sed -i "s/users.properties/newUsers.properties/g" ${plan_heur}.properties
		
		java -Xmx2024m -server -cp $CLASSPATH planning.main.Main ${plan_heur}.properties
		mv output.plan model.plan
		echo "Statistics computed! Capacity planning finished"
	elif [ ${plan_heur} = "Over" ] || [ ${plan_heur} = "History" ] ; then
		
		#Running planning
		for i in `seq 1 365`; do
			java -Xmx2024m -server -cp $CLASSPATH planning.main.Main david.properties
		done
		rm .je.dat
		mv output.plan model.plan
		echo "Capacity planning finished"
	fi


	#Running workload execution. Here an year (365 days) is being considered
	for i in `seq 1 365`; do
		java -Xmx2024m -server -cp $CLASSPATH provisioning.Main david.properties > data_${value}.output
	done

	mv model.plan model_${value}.plan
	mv model_${value}.plan data_${value}.output result_${plan_heur}
	touch model.plan	
done
