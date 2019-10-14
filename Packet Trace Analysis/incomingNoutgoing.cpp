// This file will take only the original apache logs as inputs. Thus this program doesn't require intermediate .csv
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

	// std::stringstream ss(s);
	// for (string i; ss >> i;) {
	// 	if(count<6) {res.push_back(i);count++;}	
	// 	if (ss.peek() == ',')
	// 		ss.ignore();
	// }
	return res;
}

int synOrAck(string s){ //RETURN: -1: SYN | 0: SYN,ACK | 1: ACK
	string s1 = s;
	string s2 = "[SYN]";
	string s3 = "[SYN,ACK]";
	string s4 = "[ACK]";
	if (s1.find(s2) != std::string::npos){
		return -1;
	} else if (s1.find(s3) != std::string::npos){
		return 0;
	} else if (s1.find(s4) != std::string::npos){
		return 1;
	}
	return INT_MAX;
}

int synOrFinAckOrRst(string s){
	string s1 = s;
	string s2 = "[SYN]";
	string s3 = "[FIN, ACK]";
	string s4 = "[RST]";
	if (s1.find(s2) != std::string::npos){
		return -1;
	} else if (s1.find(s3) != std::string::npos){
		return 0;
	} else if(s1.find(s4) != std::string::npos){
		return 1;
	}
	return INT_MAX;
}

string sourceip(string s){
	return giveTokens(s)[2];
}

string destip(string s){
	return giveTokens(s)[3];
}

void printSpecialMaps(map<pair<string, string>, pair<int, int>> res){
	map<pair<string, string>, pair<int, int>>::iterator itr;
	int count = 0;
	for(itr = res.begin();itr!=res.end();itr++){
		// pair<string, string> tp = itr->first;
		// cout<<tp.first;
		count++;
		/*if((itr->second).first ==0)*/ cout<<(itr->first).first << " - " << (itr->first).second << " | " << (itr->second).first << " - " << (itr->second).second<<endl;
		// if(count>20) break;
	}
	// cout<<"Total Connections: "<<count<<endl;
}

int isDestinationPort21(string s){
	string s1 = s;
	string s2 = ">  21 ";
	if (s1.find(s2) != std::string::npos){
		return 1;
	}
	return -1;
}

bool isRequestFromClient(string s){
	string s1 = s;
	string s2 = "Request";
	if(s1.find(s2) != std::string::npos){
		return true;
	} 
	return false;
}

bool isResponseToClient(string s){
	string s1 = s;
	string s2 = "Response";
	if(s1.find(s2) != std::string::npos){
		return true;
	} 
	return false;
}

void incoming(string filename){
	ifstream myfile(filename);
	string path_header_q6 = "./outputs/q6/";
	string id = filename.substr(0,filename.length()-4);
	ofstream outfile(path_header_q6 + id + "_all_incoming.csv");
	vector<double> time_list;
	vector<double> timeinterval;
	timeinterval.push_back((double)0);

	if(myfile.is_open()){
		cout<<"File opened successfully: "<<filename<<endl;
		string l;
		while(getline(myfile,l)){
			vector<string> line = giveTokens(l);
			if(isDestinationPort21(line[6])==1 || isRequestFromClient(line[6])){
				double its_time = stod(line[1].substr(1,line[1].length()-2));
				time_list.push_back(its_time);
			}
		}
		for(int i=1;i<time_list.size();i++){
			timeinterval.push_back(time_list[i]-time_list[i-1]);
		}
		outfile<<"\"Incoming Packet Time Interval\""<<endl;
		for(int i=1;i<timeinterval.size();i++){
			// cout<<"\"" << timeinterval[i] << "\"" << endl;
			outfile<<"\"" << timeinterval[i] << "\"" << endl;
		}
	} else {
		cout<<"Cannot open file: "<<filename<<endl;
	}
	myfile.close();
	cout<<"File: "<<id + "_all_incoming "<<" written successfully."<<endl;
	outfile.close();
}

void packetLengthIncoming(string filename){
	ifstream myfile(filename);
	string path_header_q7 = "./outputs/q7/";
	string id = filename.substr(0,filename.length()-4);
	ofstream outfile(path_header_q7 + id + "_incoming_packet_length.csv");
	vector<int> packetLength_list;

	if(myfile.is_open()){
		cout<<"File opened successfully: "<<filename<<endl;
		string l;
		while(getline(myfile,l)){
			vector<string> line = giveTokens(l);
			if(isDestinationPort21(line[6])==1 || isRequestFromClient(line[6])){
				int length = stoi(line[5].substr(1,line[5].length()-2));
				packetLength_list.push_back(length);
			}
		}
		outfile<<"\"Incoming Packet Length\""<<endl;
		for(int i=1;i<packetLength_list.size();i++){
			outfile<<"\"" << packetLength_list[i] << "\"" << endl;
		}
	} else {
		cout<<"Cannot open file: "<<filename<<endl;
	}
	myfile.close();
	cout<<"File: "<<id + "_incoming_packet_length "<<" written successfully."<<endl;
	outfile.close();
}

void packetLengthOutgoing(string filename){
	ifstream myfile(filename);
	string path_header_q7 = "./outputs/q7/";
	string id = filename.substr(0,filename.length()-4);
	ofstream outfile(path_header_q7 + id + "_outgoing_packet_length.csv");
	vector<int> packetLength_list;

	if(myfile.is_open()){
		cout<<"File opened successfully: "<<filename<<endl;
		string l;
		getline(myfile,l);
		while(getline(myfile,l)){
			vector<string> line = giveTokens(l);
			if(!(isDestinationPort21(line[6])==1 || isRequestFromClient(line[6]))){
				// cout<<(line[5].substr(1,line[5].length()-2))<<endl;
				int length = stoi(line[5].substr(1,line[5].length()-2));
				// outfile<<"\"" << length << "\"" <<endl;
				packetLength_list.push_back(length);
			}
		}
		outfile<<"\"Outgoing Packet Length\""<<endl;
		for(int i=1;i<packetLength_list.size();i++){
			outfile<<"\"" << packetLength_list[i] << "\"" << endl;
		}
	} else {
		cout<<"Cannot open file: "<<filename<<endl;
	}
	myfile.close();
	cout<<"File: "<<id + "_outgoing_packet_length "<<" written successfully."<<endl;
	outfile.close();
}

int main(int argc, char const *argv[])
{
	vector<string> s = {{"lbnl.anon-ftp.03-01-11"},{"lbnl.anon-ftp.03-01-14"},{"lbnl.anon-ftp.03-01-18"}}; 
	for(int i=0;i<s.size();i++){incoming(s[i] + ".csv");}
	for(int i=0;i<s.size();i++){packetLengthIncoming(s[i] + ".csv");}
	for(int i=0;i<s.size();i++){packetLengthOutgoing(s[i] + ".csv");}
	return 0;
}
