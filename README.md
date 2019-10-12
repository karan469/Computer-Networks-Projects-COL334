# Computer-Networks-Projects
Course projects of Computer Networks (COL334/672) at IIT Delhi

Assignment-3

Packet Trace Instructions:

1. Place the 3 apache log files into the directory same as .cpp
2. run g++ -o first csvparser.cpp ; ./first
3. run g++ -o second histogram_generator.cpp ; ./second

These instructions will produce multiple csv files:
1. *_tcp_histogram.csv: This will be used to plot histogram, mentioned in Q3.
2. *_unique_client.csv: This will be used to answer unique client IPs.
3. *_unique_server.csv: This will be used to answer unique server IPs.
4. *_unique_flow.csv: This will be used to count number of unique TCP flows.

Assignment - 2

After the RECEIVED message
Client -> Server: "PUBLICKEY: <key in form of string>\n"

When Sending
Client -> Server: "GET PUBLICKEY <username>\n"
Server -> Client: "PUBLICKEY: <key>\n"
