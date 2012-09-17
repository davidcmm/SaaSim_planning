#!/usr/bin/Rscript

data <- read.table("consumption.dat")


#Gráfico de consumo da horas ao longo do ano
jpeg("consumoRecursos_1.jpg")

plot(data$V4[1:8760], type="l", xlab="Hora de simulação", ylab="Total de recursos utilizados", main="Recursos utilizados na simulação") #Total
lines(data$V2[1:8760], col="red") #Reserved
lines(data$V3[1:8760], col="green") #On-demand

#pch=c(1, 4, 6)
legend(-40, 120, c("Total", "Reservado", "Sob demanda"), lty=1, col=c("black", "red", "green") )

dev.off()

jpeg("consumoRecursos_2.jpg")

plot(data$V4[8761:17520], type="l", xlab="Hora de simulação", ylab="Total de recursos utilizados", main="Recursos utilizados na simulação") #Total
lines(data$V2[8761:17520], col="red") #Reserved
lines(data$V3[8761:17520], col="green") #On-demand
legend(-40, 120, c("Total", "Reservado", "Sob demanda"), lty=1, col=c("black", "red", "green") )

dev.off()

jpeg("consumoRecursos_3.jpg")

plot(data$V4[17521:26280], type="l", xlab="Hora de simulação", ylab="Total de recursos utilizados", main="Recursos utilizados na simulação") #Total
lines(data$V2[17521:26280], col="red") #Reserved
lines(data$V3[17521:26280], col="green") #On-demand
legend(-40, 120, c("Total", "Reservado", "Sob demanda"), lty=1, col=c("black", "red", "green") )

dev.off()


#Gráfico de porcentagens de utilização por quantidades de recursos reservados
jpeg("utilizacaoMaquinas_1.jpg")

on <- read.table("onDemand_1.dat")
res <- read.table("reserved_1.dat")

res$V3 <- res$V2 / 8760
on$V3 <- on$V2 / 8760

plot(ecdf(on$V3 * 100), col="green", pch=1, xlab="Percentual de utilização", ylab="CDF", main="CDF da utilização percentual dos recursos")
lines(ecdf(res$V3 * 100), col="red", pch=8, cex=2)

legend(70, 0.2, c("Reservado", "Sob demanda"), lty=1, pch=c(8, 1), col=c("red", "green") )

dev.off()

jpeg("utilizacaoMaquinas_2.jpg")

on <- read.table("onDemand_2.dat")
res <- read.table("reserved_2.dat")

res$V3 <- res$V2 / 8760
on$V3 <- on$V2 / 8760

plot(ecdf(on$V3 * 100), col="green", pch=1, xlab="Percentual de utilização", ylab="CDF", main="CDF da utilização percentual dos recursos")
lines(ecdf(res$V3 * 100), col="red", pch=8, cex=2)

legend(70, 0.2, c("Reservado", "Sob demanda"), lty=1, pch=c(8, 1), col=c("red", "green") )

dev.off()

jpeg("utilizacaoMaquinas_3.jpg")

on <- read.table("onDemand_3.dat")
res <- read.table("reserved_3.dat")

res$V3 <- res$V2 / 8760
on$V3 <- on$V2 / 8760

plot(ecdf(on$V3 * 100), col="green", pch=1, xlab="Percentual de utilização", ylab="CDF", main="CDF da utilização percentual dos recursos")
lines(ecdf(res$V3 * 100), col="red", pch=8, cex=2)

legend(70, 0.2, c("Reservado", "Sob demanda"), lty=1, pch=c(8, 1), col=c("red", "green") )

dev.off()
