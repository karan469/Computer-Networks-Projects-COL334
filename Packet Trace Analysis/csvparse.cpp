#include <iostream>
#include <bits/stdc++.h>
#include <fstream>
#include <vector>
#include <string>

using namespace std;

// no. of unique server IP : freq. of [SYN] when previous line doesnt contain [SYN,ACK]

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

int isDestinationPort21(string s){
	string s1 = s;
	string s2 = ">  21";
	if (s1.find(s2) != std::string::npos){
		return 1;
	}
	return -1;
}

void printVectorString(vector<string> str){
	for(auto& s:str){
		cout<<s<<" | ";
	}
}

void solveCSV(string sg){
	ifstream myfile(sg);

	ofstream uniqueServerFile;
	ofstream uniqueTCPFlowFile;
	
	string header_path = "./outputs/";
	string id = sg.substr(0,sg.length()-4);
	uniqueServerFile.open(  id + "_syn.csv");
	uniqueTCPFlowFile.open( id + "_tcp_flow.csv");
	
	if(myfile.is_open()){
		// int uniqueServerIPs = 0;
		// int uniqueTCPFlows = 0; 
		string l;
		vector<string> prevLine;
		vector<string> nextLine;
		myfile>>l;
		prevLine = giveTokens(l);
		while(getline(myfile,l)){
			vector<string> line = giveTokens(l); 
			
			// //checking unique server IPs
			if(line.size()>4 && line[4]=="\"TCP\""){
				if(synOrAck(line[6])==-1 && synOrAck(prevLine[6])!=0) {
					uniqueServerFile<<l<<endl;
					// uniqueServerIPs++;
				}
				if(synOrAck(line[6])==1 && isDestinationPort21(line[6])==1){
					uniqueTCPFlowFile<<l<<endl;
					// uniqueTCPFlows++;
				}
			}
			prevLine = line;
		}
		uniqueServerFile.close();
		uniqueTCPFlowFile.close();
		
		// Now create actual unique server/client IP csv and remove this *_syn.csv files
		system(("awk -F, '!seen[$4]++' " + id + "_syn.csv" + " > " +  id + "_unique_server.csv").c_str());
		system(("awk -F, '!seen[$3]++' " + id + "_syn.csv" + " > " +  id + "_unique_client.csv").c_str());
		system(("awk -F, '!seen[$3, $4, (substr($7, 0, 11))]++' " + id + "_tcp_flow.csv" + " > " +  id + "_unique_flow.csv").c_str());

		//Destroying useless files //might be used in future code devolopment - who tf knows.
		system(("rm " +  id + "_syn.csv").c_str());
		system(("rm " +  id + "_tcp_flow.csv").c_str());
		// cout<<"No. of SYN packets: "<<uniqueServerIPs<<endl;
	} else {
		cout<<"Unable to open file.";
	}
	myfile.close();
	// No working properly
	// trafficCount(id + "_unique_flow.csv");
}

int main(int argc, char const *argv[])
{
	vector<string> s = {{"lbnl.anon-ftp.03-01-11"},{"lbnl.anon-ftp.03-01-14"},{"lbnl.anon-ftp.03-01-18"}}; 
	for(int i=0;i<s.size();i++){solveCSV(s[i] + ".csv");}
	return 0;
}
