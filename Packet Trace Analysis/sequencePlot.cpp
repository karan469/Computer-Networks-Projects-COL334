#include <iostream>
#include <bits/stdc++.h>
#include <fstream>
#include <vector>
#include <string>

using namespace std;
vector<string> giveTokens(string s){
	vector<string> res;
	string delimiter = ",";
	size_t pos = 0;
	std::string token;
	int count = 0;
	while ((pos = s.find(delimiter)) != std::string::npos && count<6) {
		 token = s.substr(0, pos);
		 count++;
		 res.push_back(token);
		 s.erase(0, pos + delimiter.length());
	}
	res.push_back(s);
	return res;
}

int synOrAck(string s){ //RETURN: -1: SYN | 0: SYN,ACK | 1: ACK
	string s1 = s;
	string s2 = "[SYN]";
	string s3 = "[SYN,ACK]";
	string s4 = "[ACK]";
	string s5 = "[RST]";
	if (s1.find(s2) != std::string::npos){
		return -1;
	} else if (s1.find(s3) != std::string::npos){
		return 0;
	} else if (s1.find(s4) != std::string::npos){
		return 1;
	}	else if (s1.find(s5) != std::string::npos){
		return 2;
	}
	return INT_MAX;
}

void sequence_plot(string filename, string client_ip, string server_ip, string source_port, string destination_port)
{
	ofstream sequenceNum;
	ofstream AcknowNum;
	ifstream myfile(filename);
	if (myfile.is_open())
	{
		sequenceNum.open("./outputs/q9/sequence.csv");
		AcknowNum.open("./outputs/q9/acknowledge.csv");
		sequenceNum << "Time,SeqNum" << endl;
		AcknowNum << "Time,SeqNum" << endl;
		string l;
		bool connectionStarted = false;
		while (getline(myfile, l))
		{
			vector<string> line = giveTokens(l); 
			if (line.size() > 4 && line[4] == "\"TCP\"")
			{
				int p = line[6].find(">");
				string portclient = line[6].substr(1,p-3);
				int q = line[6].find("[");
				string portserver = line[6].substr(p+3,q-p-4);
				if (line[2]==client_ip && portclient==source_port && line[3]==server_ip && portserver==destination_port && synOrAck(line[6]) == -1)
				{
					connectionStarted = true;
					cout << "gone" << endl;
					break;
				}
			}
		}
		if (connectionStarted)
		{
			while (getline(myfile, l))
			{
				vector<string> line = giveTokens(l); 
				if (line.size() > 4 && line[4] == "\"TCP\"")
				{
					int p = line[6].find(">");
					string portclient = line[6].substr(1,p-3);
					int q = line[6].find("[");
					string portserver = line[6].substr(p+3,q-p-4);
					if (line[2]==client_ip && portclient==source_port && line[3]==server_ip && portserver==destination_port && (synOrAck(line[6]) == -1 ||synOrAck(line[6]) == 2) )
					{
						cout << "again syn" << endl;
					}
					else if(line[2]==client_ip && portclient==source_port && line[3]==server_ip && portserver==destination_port)
					{
						cout << line[2] << " " << line[3] << " " << line[6] << endl;
						int startpos = line[6].find("Ack");
						int endpos = line[6].find("Win");
						AcknowNum << line[1] << "," << line[6].substr(startpos+4, endpos-startpos-6)<<endl;
					}
					else if(line[2]==server_ip && portclient==destination_port && line[3]==client_ip && portserver==source_port)
					{
						cout << line[2] << " " << line[3] << " " << line[6] << endl;
						int startpos = line[6].find("Seq");
						int endpos = line[6].find("Ack");
						sequenceNum << line[1] << "," << line[6].substr(startpos+4, endpos-startpos-6)<<endl;
					}
				}
			}
		}
		else
		{
			cout << "connection has not started between them" << endl;
		}
	}
}

int isSourcePort21(string s){
	string s1 = s;
	string s2 = "21  < ";
	if (s1.find(s2) != std::string::npos){
		return 1;
	}
	return -1;
}

string sourceip(string s){
	return giveTokens(s)[2];
}

string destip(string s){
	return giveTokens(s)[3];
}

string dest_port(string s){
	int startpos = s.find("21  >  ");
	int endpos = s.find("[ACK]");
	string num = s.substr(startpos+7, endpos-startpos-8);
	// cout<<"# "<<num<<endl;
	// if(s.find("[ACK]")!=string::npos){return stoi(num);}
	return num;
}

void retransmitted(string filename){
	ifstream myfile(filename);
	string id = filename.substr(0,filename.length()-4);
	map<tuple<string, string, string, string>, string> transmitted;
	string path_header_q9 = "./outputs/q9/";
	ofstream outfile(path_header_q9 + id + "_retransmitted_tuple.csv");

	if(myfile.is_open()){
		cout<<"File: "<<filename<<" opened successfully."<<endl;
		string l;
		outfile<<"\"Server_IP\",\"Client_IP\",\"Server_port\",\"Client_port\""<<endl;
		while(getline(myfile, l)){
			vector<string> line = giveTokens(l);
			string source = sourceip(l);
			string dest = destip(l);
			string port1 = dest_port(line[6]);
			string port2 = "21";
			tuple<string, string, string, string> tup = make_tuple(source, dest, port1, port2);

			map<tuple<string, string, string, string>, string>::iterator itr = transmitted.find(tup);
			
			if(isSourcePort21(line[6]) && synOrAck(line[6])==1 && itr == transmitted.end()){
				int startpos = line[6].find("Ack");
				int endpos = line[6].find("Win");
				string acknum = line[6].substr(startpos+4, endpos-startpos-6);
				acknum = acknum.substr(1,acknum.length()-2);
				transmitted.insert({tup, acknum});
				// outfile<<"\"" << source << ",\"" << dest << "\",\"" << port1 << "\",\"" << port2 << "\"" <<endl;
			}
		}
	} else {
		cout<<"Cannot open file: "<<filename<<endl;
	}
	myfile.close();
	ifstream myfile1(filename);
	if(myfile1.is_open()){
		cout<<"OPENED AGAIN"<<endl;
		string l;
		getline(myfile1,l);
		while(getline(myfile1,l)){
			vector<string> line = giveTokens(l);
			string source = sourceip(l);
			string dest = destip(l);
			string port1 = dest_port(line[6]);
			string port2 = "21";
			tuple<string, string, string, string> tup = make_tuple(source, dest, port1, port2);

			map<tuple<string, string, string, string>, string>::iterator itr;
			if(itr!=transmitted.end() && isSourcePort21(line[6]) && synOrAck(line[6])==1 && port1.find(">")==string::npos){
				outfile << source << "," << dest << ",\"" << port1 << "\",\"" << port2 << "\"" <<endl;
			}
		}
	}
}

int main(int argc, char const *argv[])
{
	/* code */
	string client_ip, server_ip, source_port, destination_port;
	// cout << "client_ip: ";cin>>client_ip;
	// cout << "server_ip: ";cin>>server_ip;
	// cout << "source_port: ";cin>>source_port;
	// cout << "destination_port: ";cin>>destination_port;
	string filename;
	// cout << "filename:";cin >>filename;
	client_ip="\"104.205.27.72\"";
	server_ip="\"128.3.28.48\"";
	source_port="42009";
	destination_port="21";
	filename="./apache_logs/lbnl.anon-ftp.03-01-11.csv";

	sequence_plot(filename, client_ip, server_ip, source_port, destination_port);


	return 0;
}
