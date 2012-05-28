Re#!/bin/Rscript
library("ggplot2")

#Avaliacao de reservas
#op_100_10 <- read.table("op_100_10/plans.dat")
#op_100_10 <- read.table("op_partial_100_10/plans.dat")
op_100_10 <- read.table("op_partial_fac5_100_10/plans.dat")
ov_100_10 <- read.table("ov_100_10/plans.dat")
ut_100_10 <- read.table("ut_100_10/plans.dat")

#op_100_5 <- read.table("op_100_5/plans.dat")
#op_100_5 <- read.table("op_partial_100_5/plans.dat")
op_100_5 <- read.table("op_partial_fac5_100_5/plans.dat")
ov_100_5 <- read.table("ov_100_5/plans.dat")
ut_100_5 <- read.table("ut_100_5/plans.dat")

#op_100_15 <- read.table("op_100_15/plans.dat")
#op_100_15 <- read.table("op_partial_100_15/plans.dat")
op_100_15 <- read.table("op_partial_fac5_100_15/plans.dat")
ov_100_15 <- read.table("ov_100_15/plans.dat")
ut_100_15 <- read.table("ut_100_15/plans.dat")

#10 usuarios
#op_10_10 <- read.table("op_10_10/plans.dat")
#op_10_10 <- read.table("op_partial_10_10/plans.dat")
op_10_10 <- read.table("op_partial_fac5_10_10/plans.dat")
ov_10_10 <- read.table("ov_10_10/plans.dat")
ut_10_10 <- read.table("ut_10_10/plans.dat")

#op_10_5 <- read.table("op_10_5/plans.dat")
#op_10_5 <- read.table("op_partial_10_5/plans.dat")
op_10_5 <- read.table("op_partial_fac5_10_5/plans.dat")
ov_10_5 <- read.table("ov_10_5/plans.dat")
ut_10_5 <- read.table("ut_10_5/plans.dat")

#op_10_15 <- read.table("op_10_15/plans.dat")
#op_10_15 <- read.table("op_partial_10_15/plans.dat")
op_10_15 <- read.table("op_partial_fac5_10_15/plans.dat")
ov_10_15 <- read.table("ov_10_15/plans.dat")
ut_10_15 <- read.table("ut_10_15/plans.dat")

#50 usuarios
#op_50_10 <- read.table("op_50_10/plans.dat")
#op_50_10 <- read.table("op_partial_50_10/plans.dat")
op_50_10 <- read.table("op_partial_fac5_50_10/plans.dat")
ov_50_10 <- read.table("ov_50_10/plans.dat")
ut_50_10 <- read.table("ut_50_10/plans.dat")

#op_50_5 <- read.table("op_50_5/plans.dat")
#op_50_5 <- read.table("op_partial_50_5/plans.dat")
op_50_5 <- read.table("op_partial_fac5_50_5/plans.dat")
ov_50_5 <- read.table("ov_50_5/plans.dat")
ut_50_5 <- read.table("ut_50_5/plans.dat")

#op_50_15 <- read.table("op_50_15/plans.dat")
#op_50_15 <- read.table("op_partial_50_15/plans.dat")
op_50_15 <- read.table("op_partial_fac5_50_15/plans.dat")
ov_50_15 <- read.table("ov_50_15/plans.dat")
ut_50_15 <- read.table("ut_50_15/plans.dat")





#postscript(file="reservas_100_5.eps", paper="special",width=10,height=10, horizontal=FALSE)
jpeg("reservas_100_5.jpg")

df <- structure(c(mean(op_100_5$V1), mean(ov_100_5$V1), mean(ut_100_5$V1), mean(op_100_5$V2*2), mean(ov_100_5$V2*2), mean(ut_100_5$V2*2), mean(op_100_5$V3*2), mean(ov_100_5$V3*2), mean(ut_100_5$V3*2), mean(op_100_5$V4*4), mean(ov_100_5$V4*4), mean(ut_100_5$V4*4)), .Dim = c(3,
     4), .Dimnames = list(c("RF", "OV", "UT"), c("small", "large", "medium",
     "xlarge")))
df.m <- melt(df)
df.m <- rename(df.m, c(X1 = "Heur", X2 = "Tipo"))
p1 <- ggplot(df.m, aes(x = Heur, y = value,
     fill = Tipo)) + opts(title = "Quantidade de núcleos reservados por tipo de máquina: 5%") +
     labs(x = "Heurísticas Utilizadas", y = "Total de núcleos reservados") 
p1 + geom_bar(stat = "identity", position = "stack")

dev.off()


#postscript(file="reservas_100_10.eps", paper="special",width=10,height=10, horizontal=FALSE)
jpeg("reservas_100_10.jpg")

df <- structure(c(mean(op_100_10$V1), mean(ov_100_10$V1), mean(ut_100_10$V1), mean(op_100_10$V2*2), mean(ov_100_10$V2*2), mean(ut_100_10$V2*2), mean(op_100_10$V3*2), mean(ov_100_10$V3*2), mean(ut_100_10$V3*2), mean(op_100_10$V4*4), mean(ov_100_10$V4*4), mean(ut_100_10$V4*4)), .Dim = c(3,
     4), .Dimnames = list(c("RF", "OV", "UT"), c("small", "large", "medium",
     "xlarge")))
df.m <- melt(df)
df.m <- rename(df.m, c(X1 = "Heur", X2 = "Tipo"))
p2 <- ggplot(df.m, aes(x = Heur, y = value,
     fill = Tipo)) + opts(title = "Quantidade de núcleos reservados por tipo de máquina: 10%") +
     labs(x = "Heurísticas Utilizadas", y = "Total de núcleos reservados")
p2 + geom_bar(stat = "identity", position = "stack")

dev.off()


#postscript(file="reservas_100_15.eps", paper="special",width=10,height=10, horizontal=FALSE)
jpeg("reservas_100_15.jpg")

df <- structure(c(mean(op_100_15$V1), mean(ov_100_15$V1), mean(ut_100_15$V1), mean(op_100_15$V2*2), mean(ov_100_15$V2*2), mean(ut_100_15$V2*2), mean(op_100_15$V3*2), mean(ov_100_15$V3*2), mean(ut_100_15$V3*2), mean(op_100_15$V4*4), mean(ov_100_15$V4*4), mean(ut_100_15$V4*4)), .Dim = c(3,
     4), .Dimnames = list(c("RF", "OV", "UT"), c("small", "large", "medium",
     "xlarge")))
df.m <- melt(df)
df.m <- rename(df.m, c(X1 = "Heur", X2 = "Tipo"))
p3 <- ggplot(df.m, aes(x = Heur, y = value,
     fill = Tipo)) + opts(title = "Quantidade de núcleos reservados por tipo de máquina: 15%") +
     labs(x = "Heurísticas Utilizadas", y = "Total de núcleos reservados") 
p3+ geom_bar(stat = "identity", position = "stack")

dev.off()


#postscript(file="reservas_50_5.eps", paper="special",width=10,height=10, horizontal=FALSE)
jpeg("reservas_50_5.jpg")

df <- structure(c(mean(op_50_5$V1), mean(ov_50_5$V1), mean(ut_50_5$V1), mean(op_50_5$V2*2), mean(ov_50_5$V2*2), mean(ut_50_5$V2*2), mean(op_50_5$V3*2), mean(ov_50_5$V3*2), mean(ut_50_5$V3*2), mean(op_50_5$V4*4), mean(ov_50_5$V4*4), mean(ut_50_5$V4*4)), .Dim = c(3,
     4), .Dimnames = list(c("RF", "OV", "UT"), c("small", "large", "medium",
     "xlarge")))
df.m <- melt(df)
df.m <- rename(df.m, c(X1 = "Heur", X2 = "Tipo"))
a <- ggplot(df.m, aes(x = Heur, y = value,
     fill = Tipo)) + opts(title = "Quantidade de núcleos reservados por tipo de máquina: 5%") +
     labs(x = "Heurísticas Utilizadas", y = "Total de núcleos reservados")
a + geom_bar(stat = "identity", position = "stack")

dev.off()

#postscript(file="reservas_50_10.eps", paper="special",width=10,height=10, horizontal=FALSE)
jpeg("reservas_50_10.jpg")

df <- structure(c(mean(op_50_10$V1), mean(ov_50_10$V1), mean(ut_50_10$V1), mean(op_50_10$V2*2), mean(ov_50_10$V2*2), mean(ut_50_10$V2*2), mean(op_50_10$V3*2), mean(ov_50_10$V3*2), mean(ut_50_10$V3*2), mean(op_50_10$V4*4), mean(ov_50_10$V4*4), mean(ut_50_10$V4*4)), .Dim = c(3,
     4), .Dimnames = list(c("RF", "OV", "UT"), c("small", "large", "medium",
     "xlarge")))
df.m <- melt(df)
df.m <- rename(df.m, c(X1 = "Heur", X2 = "Tipo"))
a <- ggplot(df.m, aes(x = Heur, y = value,
     fill = Tipo)) + opts(title = "Quantidade de núcleos reservados por tipo de máquina: 10%") +
     labs(x = "Heurísticas Utilizadas", y = "Total de núcleos reservados")
a + geom_bar(stat = "identity", position = "stack")

dev.off()

#postscript(file="reservas_50_15.eps", paper="special",width=10,height=10, horizontal=FALSE)
jpeg("reservas_50_15.jpg")

df <- structure(c(mean(op_50_15$V1), mean(ov_50_15$V1), mean(ut_50_15$V1), mean(op_50_15$V2*2), mean(ov_50_15$V2*2), mean(ut_50_15$V2*2), mean(op_50_15$V3*2), mean(ov_50_15$V3*2), mean(ut_50_15$V3*2), mean(op_50_15$V4*4), mean(ov_50_15$V4*4), mean(ut_50_15$V4*4)), .Dim = c(3,
     4), .Dimnames = list(c("RF", "OV", "UT"), c("small", "large", "medium",
     "xlarge")))
df.m <- melt(df)
df.m <- rename(df.m, c(X1 = "Heur", X2 = "Tipo"))
a <- ggplot(df.m, aes(x = Heur, y = value,
     fill = Tipo)) + opts(title = "Quantidade de núcleos reservados por tipo de máquina: 15%") +
     labs(x = "Heurísticas Utilizadas", y = "Total de núcleos reservados")
a + geom_bar(stat = "identity", position = "stack")

dev.off()


#postscript(file="reservas_10_5.eps", paper="special",width=10,height=10, horizontal=FALSE)
jpeg("reservas_10_5.jpg")

df <- structure(c(mean(op_10_5$V1), mean(ov_10_5$V1), mean(ut_10_5$V1), mean(op_10_5$V2*2), mean(ov_10_5$V2*2), mean(ut_10_5$V2*2), mean(op_10_5$V3*2), mean(ov_10_5$V3*2), mean(ut_10_5$V3*2), mean(op_10_5$V4*4), mean(ov_10_5$V4*4), mean(ut_10_5$V4*4)), .Dim = c(3,
     4), .Dimnames = list(c("RF", "OV", "UT"), c("small", "large", "medium",
     "xlarge")))
df.m <- melt(df)
df.m <- rename(df.m, c(X1 = "Heur", X2 = "Tipo"))
a <- ggplot(df.m, aes(x = Heur, y = value,
     fill = Tipo)) + opts(title = "Quantidade de núcleos reservados por tipo de máquina: 5%") +
     labs(x = "Heurísticas Utilizadas", y = "Total de núcleos reservados")
a + geom_bar(stat = "identity", position = "stack")

dev.off()

#postscript(file="reservas_10_10.eps", paper="special",width=10,height=10, horizontal=FALSE)
jpeg("reservas_10_10.jpg")

df <- structure(c(mean(op_10_10$V1), mean(ov_10_10$V1), mean(ut_10_10$V1), mean(op_10_10$V2*2), mean(ov_10_10$V2*2), mean(ut_10_10$V2*2), mean(op_10_10$V3*2), mean(ov_10_10$V3*2), mean(ut_10_10$V3*2), mean(op_10_10$V4*4), mean(ov_10_10$V4*4), mean(ut_10_10$V4*4)), .Dim = c(3,
     4), .Dimnames = list(c("RF", "OV", "UT"), c("small", "large", "medium",
     "xlarge")))
df.m <- melt(df)
df.m <- rename(df.m, c(X1 = "Heur", X2 = "Tipo"))
a <- ggplot(df.m, aes(x = Heur, y = value,
     fill = Tipo)) + opts(title = "Quantidade de núcleos reservados por tipo de máquina: 10%") +
     labs(x = "Heurísticas Utilizadas", y = "Total de núcleos reservados")
a + geom_bar(stat = "identity", position = "stack")

dev.off()

#postscript(file="reservas_10_15.eps", paper="special",width=10,height=10, horizontal=FALSE)
jpeg("reservas_10_15.jpg")

df <- structure(c(mean(op_10_15$V1), mean(ov_10_15$V1), mean(ut_10_15$V1), mean(op_10_15$V2*2), mean(ov_10_15$V2*2), mean(ut_10_15$V2*2), mean(op_10_15$V3*2), mean(ov_10_15$V3*2), mean(ut_10_15$V3*2), mean(op_10_15$V4*4), mean(ov_10_15$V4*4), mean(ut_10_15$V4*4)), .Dim = c(3,
     4), .Dimnames = list(c("RF", "OV", "UT"), c("small", "large", "medium",
     "xlarge")))
df.m <- melt(df)
df.m <- rename(df.m, c(X1 = "Heur", X2 = "Tipo"))
a <- ggplot(df.m, aes(x = Heur, y = value,
     fill = Tipo)) + opts(title = "Quantidade de núcleos reservados por tipo de máquina: 15%") +
     labs(x = "Heurísticas Utilizadas", y = "Total de núcleos reservados")
a + geom_bar(stat = "identity", position = "stack")

dev.off()


#>>>>>>>>>> Analise de consumo de horas 100 usuarios
#postscript(file="consumo_100.eps", paper="special",width=10,height=10, horizontal=FALSE)
jpeg("consumo_100.jpg")
par(mfrow=c(3, 3))

reserved <- c(1:70)
ondemand <- c(1:70)

#op_100_10 <- read.table("op_100_10/consumption.dat")
#op_100_10 <- read.table("op_partial_100_10/consumption.dat")
op_100_10 <- read.table("op_partial_fac5_100_10/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(op_100_10[initial:end, 1120]+op_100_10[initial:end, 1125] + op_100_10[initial:end, 1130]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(op_100_10[initial:end, 1118]+op_100_10[initial:end, 1123] + op_100_10[initial:end, 1128]))
	initial = initial + 12
	end = end + 12
}

#dados <- data.frame(total=reserved+ondemand, reservado=reserved, sobdemanda=ondemand, scen=c(1:70))
#a <- ggplot(data=dados)
#a+geom_line(aes(y=total, x=scen, colour="Total")) + opts(title = "Consumo de horas") +
#     labs(x = "Cenários de Simulação", y = "Total de CPU-hr consumido")
#last_plot()+geom_line(aes(y=reservado, x=scen, colour="Reservado"))
#last_plot()+geom_line(aes(y=sobdemanda, x=scen, colour="Sob demanda"))
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="OP 100 us 10%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)


ov_100_10 <- read.table("ov_100_10/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(ov_100_10[initial:end, 1120]+ov_100_10[initial:end, 1125] + ov_100_10[initial:end, 1130]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(ov_100_10[initial:end, 1118]+ov_100_10[initial:end, 1123] + ov_100_10[initial:end, 1128]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="OV 100 us 10%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)



ut_100_10 <- read.table("ut_100_10/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(ut_100_10[initial:end, 1120]+ut_100_10[initial:end, 1125] + ut_100_10[initial:end, 1130]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(ut_100_10[initial:end, 1118]+ut_100_10[initial:end, 1123] + ut_100_10[initial:end, 1128]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="UT 100 us 10%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)


#op_100_5 <- read.table("op_100_5/consumption.dat")
#op_100_5 <- read.table("op_partial_100_5/consumption.dat")
op_100_5 <- read.table("op_partial_fac5_100_5/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(op_100_5[initial:end, 1120]+op_100_5[initial:end, 1125] + op_100_5[initial:end, 1130]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(op_100_5[initial:end, 1118]+op_100_5[initial:end, 1123] + op_100_5[initial:end, 1128]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="OP 100 us 5%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)


ov_100_5 <- read.table("ov_100_5/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(ov_100_5[initial:end, 1120]+ov_100_5[initial:end, 1125] + ov_100_5[initial:end, 1130]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(ov_100_5[initial:end, 1118]+ov_100_5[initial:end, 1123] + ov_100_5[initial:end, 1128]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="OV 100 us 5%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)


ut_100_5 <- read.table("ut_100_5/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(ut_100_5[initial:end, 1120]+ut_100_5[initial:end, 1125] + ut_100_5[initial:end, 1130]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(ut_100_5[initial:end, 1118]+ut_100_5[initial:end, 1123] + ut_100_5[initial:end, 1128]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="UT 100 us 5%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)


#op_100_15 <- read.table("op_100_15/consumption.dat")
#op_100_15 <- read.table("op_partial_100_15/consumption.dat")
op_100_15 <- read.table("op_partial_fac5_100_15/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(op_100_15[initial:end, 1120]+op_100_15[initial:end, 1125] + op_100_15[initial:end, 1130]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(op_100_15[initial:end, 1118]+op_100_15[initial:end, 1123] + op_100_15[initial:end, 1128]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="OP 100 us 15%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)

ov_100_15 <- read.table("ov_100_15/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(ov_100_15[initial:end, 1120]+ov_100_15[initial:end, 1125] + ov_100_15[initial:end, 1130]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(ov_100_15[initial:end, 1118]+ov_100_15[initial:end, 1123] + ov_100_15[initial:end, 1128]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="OV 100 us 15%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)


ut_100_15 <- read.table("ut_100_15/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(ut_100_15[initial:end, 1120]+ut_100_15[initial:end, 1125] + ut_100_15[initial:end, 1130]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(ut_100_15[initial:end, 1118]+ut_100_15[initial:end, 1123] + ut_100_15[initial:end, 1128]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="UT 100 us 15%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)

par(xpd=NA)
#legend(locator(1), c("Total", "Reservado", "Sob demanda"), pch=19, col= c("red", "blue", "black"))
legend(-40, 500000, c("Total", "Reservado", "Sob demanda"), pch=c(1, 4, 6), col=c("red", "blue", "black") )

dev.off()





#>>>>>>>>>> Analise de consumo de horas 50 usuarios
#postscript(file="consumo_50.eps", paper="special",width=10,height=10, horizontal=FALSE)
jpeg("consumo_50.jpg")
par(mfrow=c(3, 3))

reserved <- c(1:70)
ondemand <- c(1:70)

#op_50_10 <- read.table("op_50_10/consumption.dat")
#op_50_10 <- read.table("op_partial_50_10/consumption.dat")
op_50_10 <- read.table("op_partial_fac5_50_10/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(op_50_10[initial:end, 570]+op_50_10[initial:end, 575] + op_50_10[initial:end, 580]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(op_50_10[initial:end, 568]+op_50_10[initial:end, 573] + op_50_10[initial:end, 578]))
	initial = initial + 12
	end = end + 12
}

plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="OP 50 us 10%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)


ov_50_10 <- read.table("ov_50_10/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(ov_50_10[initial:end, 570]+ov_50_10[initial:end, 575] + ov_50_10[initial:end, 580]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(ov_50_10[initial:end, 568]+ov_50_10[initial:end, 573] + ov_50_10[initial:end, 578]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="OV 50 us 10%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)



ut_50_10 <- read.table("ut_50_10/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(ut_50_10[initial:end, 570]+ut_50_10[initial:end, 575] + ut_50_10[initial:end, 580]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(ut_50_10[initial:end, 568]+ut_50_10[initial:end, 573] + ut_50_10[initial:end, 578]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="UT 50 us 10%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)


#op_50_5 <- read.table("op_50_5/consumption.dat")
#op_50_5 <- read.table("op_partial_50_5/consumption.dat")
op_50_5 <- read.table("op_partial_fac5_50_5/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(op_50_5[initial:end, 570]+op_50_5[initial:end, 575] + op_50_5[initial:end, 580]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(op_50_5[initial:end, 568]+op_50_5[initial:end, 573] + op_50_5[initial:end, 578]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="OP 50 us 5%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)


ov_50_5 <- read.table("ov_50_5/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(ov_50_5[initial:end, 570]+ov_50_5[initial:end, 575] + ov_50_5[initial:end, 580]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(ov_50_5[initial:end, 568]+ov_50_5[initial:end, 573] + ov_50_5[initial:end, 578]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="OV 50 us 5%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)


ut_50_5 <- read.table("ut_50_5/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(ut_50_5[initial:end, 570]+ut_50_5[initial:end, 575] + ut_50_5[initial:end, 580]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(ut_50_5[initial:end, 568]+ut_50_5[initial:end, 573] + ut_50_5[initial:end, 578]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="UT 50 us 5%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)


#op_50_15 <- read.table("op_50_15/consumption.dat")
#op_50_15 <- read.table("op_partial_50_15/consumption.dat")
op_50_15 <- read.table("op_partial_fac5_50_15/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(op_50_15[initial:end, 570]+op_50_15[initial:end, 575] + op_50_15[initial:end, 580]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(op_50_15[initial:end, 568]+op_50_15[initial:end, 573] + op_50_15[initial:end, 578]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="OP 50 us 15%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)

ov_50_15 <- read.table("ov_50_15/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(ov_50_15[initial:end, 570]+ov_50_15[initial:end, 575] + ov_50_15[initial:end, 580]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(ov_50_15[initial:end, 568]+ov_50_15[initial:end, 573] + ov_50_15[initial:end, 578]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="OV 50 us 15%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)


ut_50_15 <- read.table("ut_50_15/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(ut_50_15[initial:end, 570]+ut_50_15[initial:end, 575] + ut_50_15[initial:end, 580]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(ut_50_15[initial:end, 568]+ut_50_15[initial:end, 573] + ut_50_15[initial:end, 578]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="UT 50 us 15%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)

par(xpd=NA)
#legend(locator(1), c("Total", "Reservado", "Sob demanda"), pch=19, col= c("red", "blue", "black"))
legend(-40, 500000, c("Total", "Reservado", "Sob demanda"), pch=c(1, 4, 6), col=c("red", "blue", "black") )

dev.off()








#>>>>>>>>>> Analise de consumo de horas 10 usuarios
#postscript(file="consumo_10.eps", paper="special",width=10,height=10, horizontal=FALSE)
jpeg("consumo_10.jpg")
par(mfrow=c(3, 3))

reserved <- c(1:70)
ondemand <- c(1:70)

#op_10_10 <- read.table("op_10_10/consumption.dat")
#op_10_10 <- read.table("op_partial_10_10/consumption.dat")
op_10_10 <- read.table("op_partial_fac5_10_10/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(op_10_10[initial:end, 130]+op_10_10[initial:end, 135] + op_10_10[initial:end, 140]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(op_10_10[initial:end, 128]+op_10_10[initial:end, 133] + op_10_10[initial:end, 138]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="OP 10 us 10%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)


ov_10_10 <- read.table("ov_10_10/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(ov_10_10[initial:end, 130]+ov_10_10[initial:end, 135] + ov_10_10[initial:end, 140]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(ov_10_10[initial:end, 128]+ov_10_10[initial:end, 133] + ov_10_10[initial:end, 138]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="OV 10 us 10%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)



ut_10_10 <- read.table("ut_10_10/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(ut_10_10[initial:end, 130]+ut_10_10[initial:end, 135] + ut_10_10[initial:end, 140]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(ut_10_10[initial:end, 128]+ut_10_10[initial:end, 133] + ut_10_10[initial:end, 138]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="UT 10 us 10%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)


#op_10_5 <- read.table("op_10_5/consumption.dat")
#op_10_5 <- read.table("op_partial_10_5/consumption.dat")
op_10_5 <- read.table("op_partial_fac5_10_5/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(op_10_5[initial:end, 130]+op_10_5[initial:end, 135] + op_10_5[initial:end, 140]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(op_10_5[initial:end, 128]+op_10_5[initial:end, 133] + op_10_5[initial:end, 138]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="OP 10 us 5%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)


ov_10_5 <- read.table("ov_10_5/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(ov_10_5[initial:end, 130]+ov_10_5[initial:end, 135] + ov_10_5[initial:end, 140]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(ov_10_5[initial:end, 128]+ov_10_5[initial:end, 133] + ov_10_5[initial:end, 138]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="OV 10 us 5%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)


ut_10_5 <- read.table("ut_10_5/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(ut_10_5[initial:end, 130]+ut_10_5[initial:end, 135] + ut_10_5[initial:end, 140]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(ut_10_5[initial:end, 128]+ut_10_5[initial:end, 133] + ut_10_5[initial:end, 138]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="UT 10 us 5%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)


#op_10_15 <- read.table("op_10_15/consumption.dat")
#op_10_15 <- read.table("op_partial_10_15/consumption.dat")
op_10_15 <- read.table("op_partial_fac5_10_15/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(op_10_15[initial:end, 130]+op_10_15[initial:end, 135] + op_10_15[initial:end, 140]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(op_10_15[initial:end, 128]+op_10_15[initial:end, 133] + op_10_15[initial:end, 138]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="OP 10 us 15%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)



ov_10_15 <- read.table("ov_10_15/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(ov_10_15[initial:end, 130]+ov_10_15[initial:end, 135] + ov_10_15[initial:end, 140]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(ov_10_15[initial:end, 128]+ov_10_15[initial:end, 133] + ov_10_15[initial:end, 138]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="OV 10 us 15%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)

ut_10_15 <- read.table("ut_10_15/consumption.dat")
#Total reserved hrs
initial <- 1
end <- 12
for (i in 1:70) {
	reserved[i] <- sum(as.numeric(ut_10_15[initial:end, 130]+ut_10_15[initial:end, 135] + ut_10_15[initial:end, 140]))
	initial = initial + 12
	end = end + 12
}

#Total on-demand hrs
initial <- 1
end <- 12
for (i in 1:70) {
	ondemand[i] <- sum(as.numeric(ut_10_15[initial:end, 128]+ut_10_15[initial:end, 133] + ut_10_15[initial:end, 138]))
	initial = initial + 12
	end = end + 12
}
plot(reserved+ondemand, col="red", type="l", pch=1, xlab="Repetições", ylab="Total de horas consumidas", main="UT 10 us 15%", ylim=c(0, 350000))
lines(reserved, col="blue", type="l", pch=4)
lines(ondemand, col="black", type="l", pch=6)


par(xpd=NA)
#legend(locator(1), c("Total", "Reservado", "Sob demanda"), pch=19, col= c("red", "blue", "black"))
legend(-40, 500000, c("Total", "Reservado", "Sob demanda"), pch=c(1, 4, 6), col=c("red", "blue", "black") )

dev.off()
