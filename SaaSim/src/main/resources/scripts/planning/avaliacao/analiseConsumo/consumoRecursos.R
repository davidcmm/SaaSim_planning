#!/usr/bin/Rscript

#Scenario 1
put_40 <- 58
ut_40 <- read.table("ut_40/consumption.dat")
put_m40 <- 13
ut_m40 <- read.table("ut_m40/consumption.dat")

pop_40 <- 38
op_40 <- read.table("optimal_40/consumption.dat")
pop_m40 <- 5
op_m40 <- read.table("optimal_m40/consumption.dat")

pov_40 <- 45 * 2
pov_m40 <- 10 * 2

#Erro +40%

jpeg("consumoRecursos_40.jpg")

plot(op_40$V3[1:8760], col="black", typ="l", lty=1, xlab="Hora de simulação", ylab="Núcleos", main="Consumo de núcleos computacionais", ylim=c(0, 100))
abline(h=put_40, col="red", lty=2, lwd=2)
abline(h=pop_40, col="green", lty=5, lwd=2)
abline(h=pov_40, col="blue", lty=3, lwd=2)
legend(0, 80, c("Total", "UT", "RF", "SUPER"), lty=c(1, 2,5,3), col=c("black", "red", "green", "blue"), lwd=c(1, 2,2,2) )

#Erro -40%

jpeg("consumoRecursos_m40.jpg")

plot(op_40$V3[1:8760], col="black", typ="l", lty=1, xlab="Hora de simulação", ylab="Recursos", main="Consumo de núcleos computacionais", ylim=c(0, 100))
abline(h=put_m40, col="red", lty=2, lwd=2)
abline(h=pop_m40, col="green", lty=5, lwd=2)
abline(h=pov_m40, col="blue", lty=3, lwd=2)
legend(0, 80, c("Total", "UT", "RF", "SUPER"), lty=c(1,2,5,3), col=c("black", "red", "green", "blue"), lwd=c(1, 2,2,2) )
