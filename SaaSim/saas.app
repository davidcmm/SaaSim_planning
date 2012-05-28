############################## Simulator properties ##################################

# Do not define to load default value "commons.sim.util.SimpleApplicationFactory"
saas.application.factoryclass=commons.sim.util.SimpleApplicationFactory

# Application description.
saas.application.numberoftiers=1

# From now on one must describe each tier. In case none description is given,
# tier heuristic will be round robin, starting with replica and unlimited.
# For each tier 3 properties might be set: heuristic

# Provide a list with saas.application.numberoftiers comma-separated values. Leave empty to load default.
# Possible values are:
# * ROUNDROBIN = loads commons.saas.schedulingheuristics.RoundRobinHeuristic (this is the default option)
# * ROUNDROBIN_HET = loads commons.saas.schedulingheuristics.RoundRobinHeuristicForHeterogenousMachines
# * RANJAN = loads commons.saas.schedulingheuristics.RanjanHeuristic
# * CUSTOM = please provide a value to saas.application.heuristicclass
saas.application.heuristic=ROUNDROBIN_HET
#saas.application.heuristicclass=

#Number of machines to be started in the beginning of simulation
saas.application.startreplicas=35

#Maximum number of machines in the IT infrastructure
saas.application.maxreplicas=35

# Time in milliseconds between machine start and application becomes up and running.
saas.setuptime=300000

#Application response time in SLA
saas.sla.maxrt=8000

######################################################################################
