#!/bin/Rscript

library(agricolae)

#Profits Erro 40% e -40%
on_100_10 <- read.table("erro_40/on_100_10/profits.dat")$V1
on_100_15 <- read.table("erro_40/on_100_15/profits.dat")$V1
on_100_5 <- read.table("erro_40/on_100_5/profits.dat")$V1

op_100_10_40 <- read.table("erro_40/op_partial_fac5_100_10/profits.dat")$V1
ut_100_10_40 <- read.table("erro_40/ut_100_10/profits.dat")$V1
ov_100_10_40 <- read.table("erro_40/ov_100_10/profits.dat")$V1
op_100_5_40 <- read.table("erro_40/op_partial_fac5_100_5/profits.dat")$V1
ut_100_5_40 <- read.table("erro_40/ut_100_5/profits.dat")$V1
ov_100_5_40 <- read.table("erro_40/ov_100_5/profits.dat")$V1
op_100_15_40 <- read.table("erro_40/op_partial_fac5_100_15/profits.dat")$V1
ut_100_15_40 <- read.table("erro_40/ut_100_15/profits.dat")$V1
ov_100_15_40 <- read.table("erro_40/ov_100_15/profits.dat")$V1

op_100_10_m40 <- read.table("erro_m40/op_partial_fac5_100_10/profits.dat")$V1
ut_100_10_m40 <- read.table("erro_m40/ut_100_10/profits.dat")$V1
ov_100_10_m40 <- read.table("erro_m40/ov_100_10/profits.dat")$V1
op_100_5_m40 <- read.table("erro_40/op_partial_fac5_100_5/profits.dat")$V1
ut_100_5_m40 <- read.table("erro_40/ut_100_5/profits.dat")$V1
ov_100_5_m40 <- read.table("erro_40/ov_100_5/profits.dat")$V1
op_100_15_m40 <- read.table("erro_40/op_partial_fac5_100_15/profits.dat")$V1
ut_100_15_m40 <- read.table("erro_40/ut_100_15/profits.dat")$V1
ov_100_15_m40 <- read.table("erro_40/ov_100_15/profits.dat")$V1

#Hipótese nula SUPER=ON=OV=UT +40%

paste("Anova +40% 10%")
valores = data.frame(on_100_10, op_100_10_40, ut_100_10_40, ov_100_10_40)
valores=stack(valores) 
oneway.test(values ~ ind, data=valores, var.equal=T)
r1=aov(values~ind,data=valores)	
scheffe.test(r1,"ind", group=FALSE)
qf(.95, 3, 276)

paste("Anova +40% 15%")
valores = data.frame(on_100_15, op_100_15_40, ut_100_15_40, ov_100_15_40)
valores=stack(valores) 
oneway.test(values ~ ind, data=valores, var.equal=T)
r1=aov(values~ind,data=valores)	
scheffe.test(r1,"ind", group=FALSE)
qf(.95, 3, 276)

paste("Anova +40% 5%")
valores = data.frame(on_100_5, op_100_5_40, ut_100_5_40, ov_100_5_40)
valores=stack(valores) 
oneway.test(values ~ ind, data=valores, var.equal=T)
r1=aov(values~ind,data=valores)	
scheffe.test(r1,"ind", group=FALSE)
qf(.95, 3, 276)

# valor oneway.test é maior que a distribuição F => aceita que há diferença



#Hipótese nula SUPER=ON=OV=UT -40%

paste("Anova -40% 10%")

valores = data.frame(on_100_10, op_100_10_m40, ut_100_10_m40, ov_100_10_m40)
valores=stack(valores) 
oneway.test(values ~ ind, data=valores, var.equal=T)
r1=aov(values~ind,data=valores)	
scheffe.test(r1,"ind", group=FALSE)
qf(.95, 3, 276)

paste("Anova -40% 15%")
valores = data.frame(on_100_15, op_100_15_m40, ut_100_15_m40, ov_100_15_m40)
valores=stack(valores) 
oneway.test(values ~ ind, data=valores, var.equal=T)
r1=aov(values~ind,data=valores)	
scheffe.test(r1,"ind", group=FALSE)
qf(.95, 3, 276)

paste("Anova -40% 5%")
valores = data.frame(on_100_5, op_100_5_m40, ut_100_5_m40, ov_100_5_m40)
valores=stack(valores) 
oneway.test(values ~ ind, data=valores, var.equal=T)
r1=aov(values~ind,data=valores)	
scheffe.test(r1,"ind", group=FALSE)
qf(.95, 3, 276)

# valor oneway.test é maior que a distribuição F => aceita que há diferença


#Pairwise-t.test

paste("Pairwise +40% 10%: alt = diff, less, greater")
valores = data.frame(op=op_100_10_40, ut=ut_100_10_40, ov=ov_100_10_40, on=on_100_10)
valores = stack(valores)
attach(valores)
pairwise.t.test(values, ind, p.adj="bonf", alternative="two.sided", paired=TRUE)
pairwise.t.test(values, ind, p.adj="bonf", alternative="less", paired=TRUE)
pairwise.t.test(values, ind, p.adj="bonf", alternative="greater", paired=TRUE)

paste("Pairwise +40% 5%")
valores = data.frame(op=op_100_5_40, ut=ut_100_5_40, ov=ov_100_5_40, on=on_100_5)
valores = stack(valores)
attach(valores)
pairwise.t.test(values, ind, p.adj="bonf", alternative="two.sided", paired=TRUE)
pairwise.t.test(values, ind, p.adj="bonf", alternative="less", paired=TRUE)
pairwise.t.test(values, ind, p.adj="bonf", alternative="greater", paired=TRUE)

paste("Pairwise +40% 15%")
valores = data.frame(op=op_100_15_40, ut=ut_100_15_40, ov=ov_100_15_40, on=on_100_15)
valores = stack(valores)
attach(valores)
pairwise.t.test(values, ind, p.adj="bonf", alternative="two.sided", paired=TRUE)
pairwise.t.test(values, ind, p.adj="bonf", alternative="less", paired=TRUE)
pairwise.t.test(values, ind, p.adj="bonf", alternative="greater", paired=TRUE)

paste("Pairwise -40% 10%")
valores = data.frame(op=op_100_10_m40, ut=ut_100_10_m40, ov=ov_100_10_m40, on=on_100_10)
valores = stack(valores)
attach(valores)
pairwise.t.test(values, ind, p.adj="bonf", alternative="two.sided", paired=TRUE)
pairwise.t.test(values, ind, p.adj="bonf", alternative="less", paired=TRUE)
pairwise.t.test(values, ind, p.adj="bonf", alternative="greater", paired=TRUE)

paste("Pairwise -40% 5%")
valores = data.frame(op=op_100_5_m40, ut=ut_100_5_m40, ov=ov_100_5_m40, on=on_100_5)
valores = stack(valores)
attach(valores)
pairwise.t.test(values, ind, p.adj="bonf", alternative="two.sided", paired=TRUE)
pairwise.t.test(values, ind, p.adj="bonf", alternative="less", paired=TRUE)
pairwise.t.test(values, ind, p.adj="bonf", alternative="greater", paired=TRUE)

paste("Pairwise -40% 15%")
valores = data.frame(op=op_100_15_m40, ut=ut_100_15_m40, ov=ov_100_15_m40, on=on_100_15)
valores = stack(valores)
attach(valores)
pairwise.t.test(values, ind, p.adj="bonf", alternative="two.sided", paired=TRUE)
pairwise.t.test(values, ind, p.adj="bonf", alternative="less", paired=TRUE)
pairwise.t.test(values, ind, p.adj="bonf", alternative="greater", paired=TRUE)

