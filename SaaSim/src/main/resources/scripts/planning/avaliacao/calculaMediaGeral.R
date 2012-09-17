#!/bin/Rscript

#Profits Erro 40% e -40%
on_100_10 <- read.table("erro_40/on_100_10/profits.dat")$V1
on_100_15 <- read.table("erro_40/on_100_15/profits.dat")$V1
on_100_5 <- read.table("erro_40/on_100_5/profits.dat")$V1

op_100_10_40 <- read.table("erro_40/op_partial_fac5_100_10/profits.dat")$V1
ut_100_10_40 <- read.table("erro_40/ut_100_10/profits.dat")$V1
op_100_5_40 <- read.table("erro_40/op_partial_fac5_100_5/profits.dat")$V1
ut_100_5_40 <- read.table("erro_40/ut_100_5/profits.dat")$V1
op_100_15_40 <- read.table("erro_40/op_partial_fac5_100_15/profits.dat")$V1
ut_100_15_40 <- read.table("erro_40/ut_100_15/profits.dat")$V1

gop_100_10_40 <- (op_100_10_40 - on_100_10) / abs(on_100_10)
gut_100_10_40 <- (ut_100_10_40 - on_100_10) / abs(on_100_10)
gop_100_5_40 <- (op_100_5_40 - on_100_5) / abs(on_100_5)
gut_100_5_40 <- (ut_100_5_40 - on_100_5) / abs(on_100_5)
gop_100_15_40 <- (op_100_15_40 - on_100_15) / abs(on_100_15)
gut_100_15_40 <- (ut_100_15_40 - on_100_15) / abs(on_100_15)

on_50_10 <- read.table("erro_40/on_50_10/profits.dat")$V1
on_50_15 <- read.table("erro_40/on_50_15/profits.dat")$V1
on_50_5 <- read.table("erro_40/on_50_5/profits.dat")$V1

op_50_10_40 <- read.table("erro_40/op_partial_fac5_50_10/profits.dat")$V1
ut_50_10_40 <- read.table("erro_40/ut_50_10/profits.dat")$V1
op_50_5_40 <- read.table("erro_40/op_partial_fac5_50_5/profits.dat")$V1
ut_50_5_40 <- read.table("erro_40/ut_50_5/profits.dat")$V1
op_50_15_40 <- read.table("erro_40/op_partial_fac5_50_15/profits.dat")$V1
ut_50_15_40 <- read.table("erro_40/ut_50_15/profits.dat")$V1

gop_50_10_40 <- (op_50_10_40 - on_50_10) / abs(on_50_10)
gut_50_10_40 <- (ut_50_10_40 - on_50_10) / abs(on_50_10)
gop_50_5_40 <- (op_50_5_40 - on_50_5) / abs(on_50_5)
gut_50_5_40 <- (ut_50_5_40 - on_50_5) / abs(on_50_5)
gop_50_15_40 <- (op_50_15_40 - on_50_15) / abs(on_50_15)
gut_50_15_40 <- (ut_50_15_40 - on_50_15) / abs(on_50_15)




op_100_10_m40 <- read.table("erro_m40/op_partial_fac5_100_10/profits.dat")$V1
ut_100_10_m40 <- read.table("erro_m40/ut_100_10/profits.dat")$V1
op_100_5_m40 <- read.table("erro_m40/op_partial_fac5_100_5/profits.dat")$V1
ut_100_5_m40 <- read.table("erro_m40/ut_100_5/profits.dat")$V1
op_100_15_m40 <- read.table("erro_m40/op_partial_fac5_100_15/profits.dat")$V1
ut_100_15_m40 <- read.table("erro_m40/ut_100_15/profits.dat")$V1

gop_100_10_m40 <- (op_100_10_m40 - on_100_10) / abs(on_100_10)
gut_100_10_m40 <- (ut_100_10_m40 - on_100_10) / abs(on_100_10)
gop_100_5_m40 <- (op_100_5_m40 - on_100_5) / abs(on_100_5)
gut_100_5_m40 <- (ut_100_5_m40 - on_100_5) / abs(on_100_5)
gop_100_15_m40 <- (op_100_15_m40 - on_100_15) / abs(on_100_15)
gut_100_15_m40 <- (ut_100_15_m40 - on_100_15) / abs(on_100_15)

op_50_10_m40 <- read.table("erro_m40/op_partial_fac5_50_10/profits.dat")$V1
ut_50_10_m40 <- read.table("erro_m40/ut_50_10/profits.dat")$V1
op_50_5_m40 <- read.table("erro_m40/op_partial_fac5_50_5/profits.dat")$V1
ut_50_5_m40 <- read.table("erro_m40/ut_50_5/profits.dat")$V1
op_50_15_m40 <- read.table("erro_m40/op_partial_fac5_50_15/profits.dat")$V1
ut_50_15_m40 <- read.table("erro_m40/ut_50_15/profits.dat")$V1

gop_50_10_m40 <- (op_50_10_m40 - on_50_10) / abs(on_50_10)
gut_50_10_m40 <- (ut_50_10_m40 - on_50_10) / abs(on_50_10)
gop_50_5_m40 <- (op_50_5_m40 - on_50_5) / abs(on_50_5)
gut_50_5_m40 <- (ut_50_5_m40 - on_50_5) / abs(on_50_5)
gop_50_15_m40 <- (op_50_15_m40 - on_50_15) / abs(on_50_15)
gut_50_15_m40 <- (ut_50_15_m40 - on_50_15) / abs(on_50_15)


#Profits Erro 10% e -10%
on_100_10 <- read.table("erro_10/on_100_10/profits.dat")$V1

op_100_10_10 <- read.table("erro_10/op_partial_fac5_100_10/profits.dat")$V1
ut_100_10_10 <- read.table("erro_10/ut_100_10/profits.dat")$V1

gop_100_10_10 <- (op_100_10_10 - on_100_10) / abs(on_100_10)
gut_100_10_10 <- (ut_100_10_10 - on_100_10) / abs(on_100_10)

op_100_10_m10 <- read.table("erro_m10/op_partial_fac5_100_10/profits.dat")$V1
ut_100_10_m10 <- read.table("erro_m10/ut_100_10/profits.dat")$V1

gop_100_10_m10 <- (op_100_10_m10 - on_100_10) / abs(on_100_10)
gut_100_10_m10 <- (ut_100_10_m10 - on_100_10) / abs(on_100_10)

#Profits Erro 20% e -20%
on_100_20 <- read.table("erro_20/on_100_10/profits.dat")$V1

op_100_10_20 <- read.table("erro_20/op_partial_fac5_100_10/profits.dat")$V1
ut_100_10_20 <- read.table("erro_20/ut_100_10/profits.dat")$V1

gop_100_10_20 <- (op_100_10_20 - on_100_10) / abs(on_100_20)
gut_100_10_20 <- (ut_100_10_20 - on_100_10) / abs(on_100_20)

op_100_10_m20 <- read.table("erro_m20/op_partial_fac5_100_10/profits.dat")$V1
ut_100_10_m20 <- read.table("erro_m20/ut_100_10/profits.dat")$V1

gop_100_10_m20 <- (op_100_10_m20 - on_100_10) / abs(on_100_20)
gut_100_10_m20 <- (ut_100_10_m20 - on_100_10) / abs(on_100_20)

#Calculando lucros gerais para 100 usuarios

total_ut <- c(ut_100_10_40, ut_100_15_40, ut_100_5_40, ut_100_10_m40, ut_100_15_m40, ut_100_5_m40, ut_100_10_10, ut_100_10_m10, ut_100_10_20, ut_100_10_m20)
paste("Lucro geral UT 100: ", mean(total_ut), "[ ", 1.96*sd(total_ut)/sqrt(length(total_ut))+mean(total_ut), " : ", mean(total_ut) - 1.96*sd(total_ut)/sqrt(length(total_ut)), " ] ")

total_op <- c(op_100_10_40, op_100_15_40, op_100_5_40, op_100_10_m40, op_100_15_m40, op_100_5_m40, op_100_10_10, op_100_10_m10, op_100_10_20, op_100_10_m20)
paste("Lucro geral RF 100: ", mean(total_op), "[ ", 1.96*sd(total_op)/sqrt(length(total_op))+mean(total_op), " : ", mean(total_op) - 1.96*sd(total_op)/sqrt(length(total_op)), " ] ")

t.test(total_ut, total_op, paired=TRUE, var.equal=FALSE)
t.test(total_ut, total_op, paired=TRUE, var.equal=FALSE, alternative="less")
t.test(total_ut, total_op, paired=TRUE, var.equal=FALSE, alternative="greater")


total_ut <- c(ut_100_10_40, ut_100_15_40, ut_100_5_40, ut_100_10_m40, ut_100_15_m40, ut_100_5_m40, ut_100_10_10, ut_100_10_m10, ut_100_10_20, ut_100_10_m20, ut_50_10_40, ut_50_15_40, ut_50_5_40, ut_50_10_m40, ut_50_15_m40, ut_50_5_m40)
paste("Lucro geral UT 100 e 50: ", mean(total_ut), "[ ", 1.96*sd(total_ut)/sqrt(length(total_ut))+mean(total_ut), " : ", mean(total_ut) - 1.96*sd(total_ut)/sqrt(length(total_ut)), " ] ")

total_op <- c(op_100_10_40, op_100_15_40, op_100_5_40, op_100_10_m40, op_100_15_m40, op_100_5_m40, op_100_10_10, op_100_10_m10, op_100_10_20, op_100_10_m20, op_50_10_40, op_50_15_40, op_50_5_40, op_50_10_m40, op_50_15_m40, op_50_5_m40)
paste("Lucro geral RF 100 e 50: ", mean(total_op), "[ ", 1.96*sd(total_op)/sqrt(length(total_op))+mean(total_op), " : ", mean(total_op) - 1.96*sd(total_op)/sqrt(length(total_op)), " ] ")

t.test(total_ut, total_op, paired=TRUE, var.equal=FALSE)
t.test(total_ut, total_op, paired=TRUE, var.equal=FALSE, alternative="less")
t.test(total_ut, total_op, paired=TRUE, var.equal=FALSE, alternative="greater")

#Calculando ganhos gerais

total_gut <- c(gut_100_10_40, gut_100_15_40, gut_100_5_40, gut_100_10_m40, gut_100_15_m40, gut_100_5_m40, gut_100_10_10, gut_100_10_m10, gut_100_10_20, gut_100_10_m20)
paste("Ganho geral UT 100 : ", mean(total_gut), "[ ", 1.96*sd(total_gut)/sqrt(length(total_gut))+mean(total_gut), " : ", mean(total_gut) - 1.96*sd(total_gut)/sqrt(length(total_gut)), " ] ")

total_gop <- c(gop_100_10_40, gop_100_15_40, gop_100_5_40, gop_100_10_m40, gop_100_15_m40, gop_100_5_m40, gop_100_10_10, gop_100_10_m10, gop_100_10_20, gop_100_10_m20)
paste("Ganho geral RF 100 : ", mean(total_gop), "[ ", 1.96*sd(total_gop)/sqrt(length(total_gop))+mean(total_gop), " : ", mean(total_gop) - 1.96*sd(total_gop)/sqrt(length(total_gop)), " ] ")

wilcox.test(total_ut, total_op, paired=TRUE, var.equal=FALSE)
wilcox.test(total_ut, total_op, paired=TRUE, var.equal=FALSE, alternative="less")
wilcox.test(total_ut, total_op, paired=TRUE, var.equal=FALSE, alternative="greater")

total_gut <- c(gut_100_10_40, gut_100_15_40, gut_100_5_40, gut_100_10_m40, gut_100_15_m40, gut_100_5_m40, gut_100_10_10, gut_100_10_m10, gut_100_10_20, gut_100_10_m20, gut_50_10_40, gut_50_15_40, gut_50_5_40, gut_50_10_m40, gut_50_15_m40, gut_50_5_m40)
paste("Ganho geral UT 100 e 50: ", mean(total_gut), "[ ", 1.96*sd(total_gut)/sqrt(length(total_gut))+mean(total_gut), " : ", mean(total_gut) - 1.96*sd(total_gut)/sqrt(length(total_gut)), " ] ")

total_gop <- c(gop_100_10_40, gop_100_15_40, gop_100_5_40, gop_100_10_m40, gop_100_15_m40, gop_100_5_m40, gop_100_10_10, gop_100_10_m10, gop_100_10_20, gop_100_10_m20, gop_50_10_40, gop_50_15_40, gop_50_5_40, gop_50_10_m40, gop_50_15_m40, gop_50_5_m40)
paste("Ganho geral RF 100 e 50: ", mean(total_gop), "[ ", 1.96*sd(total_gop)/sqrt(length(total_gop))+mean(total_gop), " : ", mean(total_gop) - 1.96*sd(total_gop)/sqrt(length(total_gop)), " ] ")

wilcox.test(total_ut, total_op, paired=TRUE, var.equal=FALSE)
wilcox.test(total_ut, total_op, paired=TRUE, var.equal=FALSE, alternative="less")
wilcox.test(total_ut, total_op, paired=TRUE, var.equal=FALSE, alternative="greater")
