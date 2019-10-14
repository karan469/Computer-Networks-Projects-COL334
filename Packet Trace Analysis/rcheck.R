# Question 5 code in here
corrdata <- read.csv("D:/Sem5/ComputerNetworks/Assignments/ass3/outputs/q5/lbnl.anon-ftp.03-01-18_unique_tcp_allpackets_q5.csv", sep=",")
head(corrdata,5)
# this code used to plot the graph
library("ggpubr")
ggscatter(corrdata, x = "Duration", y = "Data_Received", 
          add = "reg.line", conf.int = TRUE, 
          cor.coef = TRUE, cor.method = "pearson",
          xlab = "Duration", ylab = "Data Received")
# get the correlation
res <- cor.test(corrdata$Data_Sent, corrdata$Data_Received, 
                method = "pearson")
res
ggscatter(corrdata, x = "Duration", y = "Data_Sent", 
          add = "reg.line", conf.int = TRUE, 
          cor.coef = TRUE, cor.method = "pearson",
          xlab = "Duration", ylab = "Data Sent")
# get the correlation
res <- cor.test(corrdata$Data_Sent, corrdata$Data_Received, 
                method = "pearson")
res
ggscatter(corrdata, x = "Data_Sent", y = "Data_Received", 
          add = "reg.line", conf.int = TRUE, 
          cor.coef = TRUE, cor.method = "pearson",
          xlab = "Data Sent", ylab = "Data Received")
# get the correlation
res <- cor.test(corrdata$Data_Sent, corrdata$Data_Received, 
                method = "pearson")
res


# Question 6
cdfofconnectionduration <- read.csv("D:/Sem5/ComputerNetworks/Assignments/ass3/outputs/q6/lbnl.anon-ftp.03-01-18_letsee_all_connectionDurationBetweenConnections.csv", sep=",")
plot(ecdf(cdfofconnectionduration[,1]), main = "Day3: cdf of interarrival time between two connections")
print(cdfofconnectionduration)


