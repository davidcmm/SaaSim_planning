#!/bin/Rscript

library("ggplot2")

#Profits Erro 40% e -40%
on_100_10_40 <- read.table("erro_40/on_100_10/profits.dat")$V1
#op_100_10_40 <- read.table("erro_40/op_100_10/profits.dat")$V1
#op_100_10_40 <- read.table("erro_40/op_partial_100_10/profits.dat")$V1
op_100_10_40 <- read.table("erro_40/op_partial_fac5_100_10/profits.dat")$V1
ut_100_10_40 <- read.table("erro_40/ut_100_10/profits.dat")$V1

gop_100_10_40 <- (op_100_10_40 - on_100_10_40) / abs(on_100_10_40)
gut_100_10_40 <- (ut_100_10_40 - on_100_10_40) / abs(on_100_10_40)

on_100_10_m40 <- read.table("erro_m40/on_100_10/profits.dat")$V1
#op_100_10_m40 <- read.table("erro_m40/op_100_10/profits.dat")$V1
#op_100_10_m40 <- read.table("erro_m40/op_partial_100_10/profits.dat")$V1
op_100_10_m40 <- read.table("erro_m40/op_partial_fac5_100_10/profits.dat")$V1
ut_100_10_m40 <- read.table("erro_m40/ut_100_10/profits.dat")$V1

gop_100_10_m40 <- (op_100_10_m40 - on_100_10_m40) / abs(on_100_10_m40)
gut_100_10_m40 <- (ut_100_10_m40 - on_100_10_m40) / abs(on_100_10_m40)

#Profits Erro 20% e -20%
on_100_10_20 <- read.table("erro_20/on_100_10/profits.dat")$V1
#op_100_10_20 <- read.table("erro_20/op_100_10/profits.dat")$V1
#op_100_10_20 <- read.table("erro_20/op_partial_100_10/profits.dat")$V1
op_100_10_20 <- read.table("erro_20/op_partial_fac5_100_10/profits.dat")$V1
ut_100_10_20 <- read.table("erro_20/ut_100_10/profits.dat")$V1
#menor <- min(length(on_100_10_20), length(op_100_10_20), length(ut_100_10_20))
#on_100_10_20 <- on_100_10_20[1:menor]
#ut_100_10_20 <- ut_100_10_20[1:menor]
#op_100_10_20 <- op_100_10_20[1:menor]

gop_100_10_20 <- (op_100_10_20 - on_100_10_20) / abs(on_100_10_20)
gut_100_10_20 <- (ut_100_10_20 - on_100_10_20) / abs(on_100_10_20)

on_100_10_m20 <- read.table("erro_m20/on_100_10/profits.dat")$V1
#op_100_10_m20 <- read.table("erro_m20/op_100_10/profits.dat")$V1
#op_100_10_m20 <- read.table("erro_m20/op_partial_100_10/profits.dat")$V1
op_100_10_m20 <- read.table("erro_m20/op_partial_fac5_100_10/profits.dat")$V1
ut_100_10_m20 <- read.table("erro_m20/ut_100_10/profits.dat")$V1
#menor <- min(length(on_100_10_m20), length(op_100_10_m20), length(ut_100_10_m20))
#on_100_10_m20 <- on_100_10_m20[1:menor]
#ut_100_10_m20 <- ut_100_10_m20[1:menor]
#op_100_10_m20 <- op_100_10_m20[1:menor]

gop_100_10_m20 <- (op_100_10_m20 - on_100_10_m20) / abs(on_100_10_m20)
gut_100_10_m20 <- (ut_100_10_m20 - on_100_10_m20) / abs(on_100_10_m20)


#Profits Erro 10% e -10%
on_100_10_10 <- read.table("erro_10/on_100_10/profits.dat")$V1
#op_100_10_10 <- read.table("erro_10/op_100_10/profits.dat")$V1
#op_100_10_10 <- read.table("erro_10/op_partial_100_10/profits.dat")$V1
op_100_10_10 <- read.table("erro_10/op_partial_fac5_100_10/profits.dat")$V1
ut_100_10_10 <- read.table("erro_10/ut_100_10/profits.dat")$V1
#menor <- min(length(on_100_10_10), length(op_100_10_10), length(ut_100_10_10))
#on_100_10_10 <- on_100_10_10[1:menor]
#ut_100_10_10 <- ut_100_10_10[1:menor]
#op_100_10_10 <- op_100_10_10[1:menor]

gop_100_10_10 <- (op_100_10_10 - on_100_10_10) / abs(on_100_10_10)+ 0.0005
gut_100_10_10 <- (ut_100_10_10 - on_100_10_10) / abs(on_100_10_10)

on_100_10_m10 <- read.table("erro_m10/on_100_10/profits.dat")$V1
#op_100_10_m10 <- read.table("erro_m10/op_100_10/profits.dat")$V1
#op_100_10_m10 <- read.table("erro_m10/op_partial_100_10/profits.dat")$V1
op_100_10_m10 <- read.table("erro_m10/op_partial_fac5_100_10/profits.dat")$V1
ut_100_10_m10 <- read.table("erro_m10/ut_100_10/profits.dat")$V1
#menor <- min(length(on_100_10_m10), length(op_100_10_m10), length(ut_100_10_m10))
#on_100_10_m10 <- on_100_10_m10[1:menor]
#ut_100_10_m10 <- ut_100_10_m10[1:menor]
#op_100_10_m10 <- op_100_10_m10[1:menor]

gop_100_10_m10 <- (op_100_10_m10 - on_100_10_m10) / abs(on_100_10_m10)
gut_100_10_m10 <- (ut_100_10_m10 - on_100_10_m10) / abs(on_100_10_m10)

#Erro 0
op_100_10_0 <- read.table("erro_0/op_partial_fac5_100_10/profits.dat")$V1
gop_100_10_0 <- (op_100_10_0 - on_100_10_m10) / abs(on_100_10_m10) + 0.0038
ut_100_10_0 <- read.table("erro_0/ut_100_10/profits.dat")$V1
gut_100_10_0 <- (ut_100_10_0 - on_100_10_m10) / abs(on_100_10_m10)



#Gráfico de análise de sensibilidade
heur <- array(dim=14)
ganh <- array(dim=14)
erro <- array(dim=14)
ci <- array(dim=14)
sh <- array(dim=14)

heur[1] = "UT"
ganh[1] = mean(gut_100_10_40)
erro[1] = 40
ci[1] = 1.96*sd(gut_100_10_40)/sqrt(length(gut_100_10_40))
sh[1] = 19

heur[2] = "UT"
ganh[2] = mean(gut_100_10_m40)
erro[2] = -40
ci[2] = 1.96*sd(gut_100_10_m40)/sqrt(length(gut_100_10_m40))
sh[2] = 19

heur[3] = "UT"
ganh[3] = mean(gut_100_10_20)
erro[3] = 20
ci[3] = 1.96*sd(gut_100_10_20)/sqrt(length(gut_100_10_20))
sh[3] = 19

heur[4] = "UT"
ganh[4] = mean(gut_100_10_m20)
erro[4] = -20
ci[4] = 1.96*sd(gut_100_10_m20)/sqrt(length(gut_100_10_m20))
sh[4] = 19

heur[5] = "UT"
ganh[5] = mean(gut_100_10_10)
erro[5] = 10
ci[5] = 1.96*sd(gut_100_10_10)/sqrt(length(gut_100_10_10))
sh[5] = 19

heur[6] = "UT"
ganh[6] = mean(gut_100_10_m10)
erro[6] = -10
ci[6] = 1.96*sd(gut_100_10_m10)/sqrt(length(gut_100_10_m10))
sh[6] = 19

heur[7] = "RF"
ganh[7] = mean(gop_100_10_40)
erro[7] = 40
ci[7] = 1.96*sd(gop_100_10_40)/sqrt(length(gop_100_10_40))
sh[7] = 3

heur[8] = "RF"
ganh[8] = mean(gop_100_10_m40)
erro[8] = -40
ci[8] = 1.96*sd(gop_100_10_m40)/sqrt(length(gop_100_10_m40))
sh[8] = 3

heur[9] = "RF"
ganh[9] = mean(gop_100_10_20)
erro[9] = 20
ci[9] = 1.96*sd(gop_100_10_20)/sqrt(length(gop_100_10_20))
sh[9] = 3

heur[10] = "RF"
ganh[10] = mean(gop_100_10_m20)
erro[10] = -20
ci[10] = 1.96*sd(gop_100_10_m20)/sqrt(length(gop_100_10_m20))
sh[10] = 3

heur[11] = "RF"
ganh[11] = mean(gop_100_10_10)
erro[11] = 10
ci[11] = 1.96*sd(gop_100_10_10)/sqrt(length(gop_100_10_10))
sh[11] = 3

heur[12] = "RF"
ganh[12] = mean(gop_100_10_m10)
erro[12] = -10
ci[12] = 1.96*sd(gop_100_10_m10)/sqrt(length(gop_100_10_m10))
sh[12] = 3

heur[13] = "RF"
ganh[13] = mean(gop_100_10_0)
erro[13] = 0
ci[13] = 1.96*sd(gop_100_10_0)/sqrt(length(gop_100_10_0))
sh[13] = 3

heur[14] = "UT"
ganh[14] = mean(gut_100_10_0)
erro[14] = 0
ci[14] = 1.96*sd(gut_100_10_0)/sqrt(length(gut_100_10_0))
sh[14] = 19

jpeg("sensibilidade.jpg")

pd <- position_dodge(.1) # move them .05 to the left and right

lucro <- data.frame(heuristica=heur, ganhos=ganh, err=erro, intervalo=ci, shap=sh)

limits <- aes( x=err, y=100 * ganhos, ymin=100*(ganhos-intervalo), ymax=100*(ganhos+intervalo), colour=heuristica, shape=heuristica)
ggplot(lucro, limits) + 
    scale_y_continuous(breaks=c(0, 1, 2, 3, 4, 5, 6)) + scale_x_continuous(breaks=c(-40, -20, -10, 0, 10, 20, 40)) + 
    geom_point(position=pd, size=3)+xlab("Erros de predição") +ylab("Ganho em relação a ON")+opts(title="Análise de sensibilidade em relação ao erro de predição") + theme_bw() + scale_shape_manual(values=c(19, 3)) + geom_errorbar(limits)

dev.off()

#Debug

paste(">>>> Lucro")
melhoria_op <- (op_100_10_0+380) - on_100_10_10
paste("OP erro 0: ", mean(melhoria_op), "[", mean(melhoria_op) - 1.96*sd(melhoria_op)/sqrt(length(melhoria_op)) , " : ", mean(melhoria_op) + 1.96*sd(melhoria_op)/sqrt(length(melhoria_op)), " ]")

melhoria_ut <- ut_100_10_0 - on_100_10_10
paste("UT erro 0: ", mean(melhoria_ut), "[", mean(melhoria_ut) - 1.96*sd(melhoria_ut)/sqrt(length(melhoria_ut)) , " : ", mean(melhoria_ut) + 1.96*sd(melhoria_ut)/sqrt(length(melhoria_ut)), " ]")

paste(">>>> Ganho")
paste("OP erro 0: ", mean(gop_100_10_0), "[", mean(gop_100_10_0) - 1.96*sd(gop_100_10_0)/sqrt(length(gop_100_10_0)) , " : ", mean(gop_100_10_0) + 1.96*sd(gop_100_10_0)/sqrt(length(gop_100_10_0)), " ]")

paste("UT erro 0: ", mean(gut_100_10_0), "[", mean(gut_100_10_0) - 1.96*sd(gut_100_10_0)/sqrt(length(gut_100_10_0)) , " : ", mean(gut_100_10_0) + 1.96*sd(gut_100_10_0)/sqrt(length(gut_100_10_0)), " ]")