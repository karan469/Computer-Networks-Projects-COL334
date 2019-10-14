q8_incoming_1 <- read.csv(file="./outputs/q8/lbnl.anon-ftp.03-01-11_incoming_packet_length.csv", header=TRUE, sep=",")
q8_incoming_4 <- read.csv(file="./outputs/q8/lbnl.anon-ftp.03-01-14_incoming_packet_length.csv", header=TRUE, sep=",")
q8_incoming_8 <- read.csv(file="./outputs/q8/lbnl.anon-ftp.03-01-18_incoming_packet_length.csv", header=TRUE, sep=",")

p8_incoming_1 <- ecdf(q8_incoming_1[[1]])
p8_incoming_4 <- ecdf(q8_incoming_4[[1]])
p8_incoming_8 <- ecdf(q8_incoming_8[[1]])

plot(p8_incoming_1, xlab = 'Incoming Packet Length', ylab = 'Frequency', main = 'Length of incoming packets sent to Servers')
plot(p8_incoming_4, xlab = 'Incoming Packet Length', ylab = 'Frequency', main = 'Length of incoming packets sent to Servers')
plot(p8_incoming_8, xlab = 'Incoming Packet Length', ylab = 'Frequency', main = 'Length of incoming packets sent to Servers')

# -----------------------------------------------------

q8_outgoing_1 <- read.csv(file="./outputs/q8/lbnl.anon-ftp.03-01-11_outgoing_packet_length.csv", header=TRUE, sep=",")
q8_outgoing_4 <- read.csv(file="./outputs/q8/lbnl.anon-ftp.03-01-14_outgoing_packet_length.csv", header=TRUE, sep=",")
q8_outgoing_8 <- read.csv(file="./outputs/q8/lbnl.anon-ftp.03-01-18_outgoing_packet_length.csv", header=TRUE, sep=",")
p8_outgoing_1 <- ecdf(q8_outgoing_1[[1]])
p8_outgoing_4 <- ecdf(q8_outgoing_4[[1]])
p8_outgoing_8 <- ecdf(q8_outgoing_8[[1]])
plot(p8_outgoing_1, xlab = 'Outgoing Packet Length', ylab = 'Frequency', main = 'Length of outgoing packets sent to Servers - Day 1')
plot(p8_outgoing_4, xlab = 'Outgoing Packet Length', ylab = 'Frequency', main = 'Length of outgoing packets sent to Servers - Day 2')
plot(p8_outgoing_8, xlab = 'Outgoing Packet Length', ylab = 'Frequency', main = 'Length of outgoing packets sent to Servers - Day 3')

q7_incoming_1 <- read.csv(file="./outputs/q7/lbnl.anon-ftp.03-01-11_all_incoming.csv", header=TRUE, sep=",")
q7_incoming_4 <- read.csv(file="./outputs/q7/lbnl.anon-ftp.03-01-14_all_incoming.csv", header=TRUE, sep=",")
q7_incoming_8 <- read.csv(file="./outputs/q7/lbnl.anon-ftp.03-01-18_all_incoming.csv", header=TRUE, sep=",")

p7_incoming_1 <- ecdf(q7_incoming_1[[1]])
p7_incoming_4 <- ecdf(q7_incoming_4[[1]])
p7_incoming_8 <- ecdf(q7_incoming_8[[1]])

plot(p7_incoming_1, xlab = 'Inter-arrival Time', ylab = 'Frequency', main = 'Inter-arrival of Incoming Packets - Day 1')
plot(p7_incoming_4, xlab = 'Inter-arrival Time', ylab = 'Frequency', main = 'Inter-arrival of Incoming Packets - Day 2')
plot(p7_incoming_8, xlab = 'Inter-arrival Time', ylab = 'Frequency', main = 'Inter-arrival of Incoming Packets - Day 3')

# -----------------------------------------------------------------------

remove_outliers <- function(x, na.rm = TRUE, ...) {
  qnt <- quantile(x, probs=c(.25, .75), na.rm = na.rm, ...)
  H <- 1.5 * IQR(x, na.rm = na.rm)
  y <- x
  y[x < (qnt[1] - H)] <- NA
  y[x > (qnt[2] + H)] <- NA
  y
}

df_1 <- read.csv("./outputs/q7/lbnl.anon-ftp.03-01-11_all_incoming.csv", header=TRUE, sep=",")
df_4 <- read.csv("./outputs/q7/lbnl.anon-ftp.03-01-14_all_incoming.csv", header=TRUE, sep=",")
df_8 <- read.csv("./outputs/q7/lbnl.anon-ftp.03-01-18_all_incoming.csv", header=TRUE, sep=",")

library("fitdistrplus")

plotdist(df_1$"Incoming_Packet_Time_Interval", histo=TRUE, demp=TRUE)
descdist(df_1$"Incoming_Packet_Time_Interval")
fe_1 <- fitdist(df_1f$"Incoming_Packet_Time_Interval", "exp")
denscomp(list(fe_1), legendtext=c("exp"))
cdfcomp(list(fe_1), legendtext=c("exp"))
qqcomp(list(fe_1), legendtext=c("exp"))

plotdist(df_4$"Incoming_Packet_Time_Interval", histo=TRUE, demp=TRUE)
descdist(df_4$"Incoming_Packet_Time_Interval")
fe_4 <- fitdist(df_4f$"Incoming_Packet_Time_Interval", "exp")
denscomp(list(fe_4), legendtext=c("exp"))
cdfcomp(list(fe_4), legendtext=c("exp"))
qqcomp(list(fe_4), legendtext=c("exp"))

plotdist(df_8$"Incoming_Packet_Time_Interval", histo=TRUE, demp=TRUE);
descdist(df_8$"Incoming_Packet_Time_Interval")
fe_8 <- fitdist(df_8$"Incoming_Packet_Time_Interval", "exp");
denscomp(list(fe_8), legendtext=c("exp"))
cdfcomp(list(fe_8), legendtext=c("exp"))
qqcomp(list(fe_8), legendtext=c("exp"))

# while (!is.null(dev.list())) Sys.sleep(1)
