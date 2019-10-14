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

int synOrFinAckOrRst(string s){
	string s1 = s;
	string s22 = "[SYN, ECN, CWR]";
	string s2 = "[SYN]";
	string s3 = "[FIN, ACK]";
	string s4 = "[RST]";
	string s5 = "[RST, ACK]";
	if (s1.find(s2) != std::string::npos || s1.find(s22) != std::string::npos){
		return -1;
	} else if (s1.find(s3) != std::string::npos){
		return 0;
	} else if(s1.find(s4) != std::string::npos){
		return 1;
	}else if(s1.find(s5) != std::string::npos){
		return 2;
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

// pair<int,int> ports(string s){
// 	int a, b;
// 	for(int i=0;i<s.length()-5;i++){
// 		if(s.substr(i,5)=="  >  "){
// 			a = stoi(s.substr(1,i-1));

// 		}
// 	}
// }

void printVectorString(vector<string> str){
	for(auto& s:str){
		cout<<s<<" | ";
	}
}

void solveCSV(string sg){
	ifstream myfile(sg);

	ofstream uniqueServerFile;
	ofstream uniqueTCPFlowFile;
	ofstream uniqueTCPAllFile;

	string header_path = "./outputs/";
	string id = sg.substr(0,sg.length()-4);
	
	if(myfile.is_open()){
		uniqueServerFile.open(id + "_syn.csv");
		uniqueTCPFlowFile.open(id + "_tcp_flow_ackonly.csv");
		uniqueTCPAllFile.open(id + "_tcp_all.csv");

		string l;
		vector<string> prevLine;
		vector<string> nextLine;
		myfile>>l;
		prevLine = giveTokens(l);
		while(getline(myfile,l)){
			vector<string> line = giveTokens(l); 
			
			// //checking unique server IPs
			if(line.size()>4 && line[4]=="\"TCP\""){
				if(synOrAck(line[6])==-1) {
					uniqueServerFile<<l<<endl;
				}
				if(synOrAck(line[6])==1 && isDestinationPort21(line[6])==1){
					uniqueTCPFlowFile<<l<<endl;
				}
				if((synOrFinAckOrRst(line[6])==-1 && isDestinationPort21(line[6])==1) || synOrFinAckOrRst(line[6])==0 || (synOrFinAckOrRst(line[6])==1) || (synOrFinAckOrRst(line[6])==2)){
					uniqueTCPAllFile<<l<<endl;
				}
			}
			prevLine = line;
		}
		uniqueServerFile.close();
		uniqueTCPFlowFile.close();
		uniqueTCPAllFile.close();
		
		// Now create actual unique server/client IP csv and remove this *_syn.csv files
		// system(("awk -F, '!seen[$4]++' " + id + "_syn.csv" + " > " +  id + "_unique_server.csv").c_str());
		// system(("awk -F, '!seen[$3]++' " + id + "_syn.csv" + " > " +  id + "_unique_client.csv").c_str());
		system(("awk -F, '!seen[$3, $4, (substr($7, 0, 11))]++' " + id + "_tcp_flow_ackonly.csv" + " > " +  id + "_unique_flow_ackonly.csv").c_str());

		system(("awk '{a[i++]=$0}END{for(j=i-1;j>=0;j--)print a[j];}' "+ id + "_tcp_all.csv" + " > " +  id + "_tcp_all1.csv").c_str());
		system(("awk -F, -v s=\"Seq\" '!seen[$3, $4, (substr($7, 0, index($7,s)-1))]++' " + id + "_tcp_all1.csv" + " > " +  "./outputs/q4/" + id + "_letsee_all1.csv").c_str()); //this assumes a directory ./outputs/q4/
		string filepath = "./outputs/q4/";
		system(("awk '{a[i++]=$0}END{for(j=i-1;j>=0;j--)print a[j];}' "+ filepath  + id + "_letsee_all1.csv" + " > " +  "./outputs/q4/" +  id + "_letsee_all.csv").c_str());

		system(("awk '{a[i++]=$0}END{for(j=i-1;j>=0;j--)print a[j];}' "+ sg + " > " +  sg + "1.csv").c_str());
		system(("awk -F, '!seen[$3, $4, $7]++' " + sg + "1.csv" + " > " +  "outputs/q5/" + id + "_unique_tcp_allpackets1.csv").c_str());
		string filepath1 = "./outputs/q5/";
		system(("awk '{a[i++]=$0}END{for(j=i-1;j>=0;j--)print a[j];}' "+ filepath1  + id + "_unique_tcp_allpackets1.csv" + " > " +  "./outputs/q5/" +  id + "_unique_tcp_allpackets.csv").c_str());
		// system(("awk -F, '!seen[$3, $4, (substr($7, 0, 11))]++' " + id + "_tcp_all.csv" + " > " +  "./outputs/q4/" + id + "_letsee_all.csv").c_str()); //this assumes a directory ./outputs/q4/
		
		// if you want to store _letsee_all.csv in home directory too.
		// system(("awk -F, '!seen[$3, $4, (substr($7, 0, 11))]++' " + id + "_tcp_all.csv" + " > " + id + "_letsee_all.csv").c_str());
		
		// this isnt working
		// system(("if test -d ./outputs/q4; then echo exist; else mkdir ./outputs/q4; fi").c_str());

		//Destroying useless files //might be used in future code devolopment - who tf knows.
		system(("rm " +  id + "_syn.csv").c_str());
		system(("rm " +  id + "_tcp_flow_ackonly.csv").c_str());
		system(("rm " +  id + "_tcp_all1.csv").c_str());
		system(("rm " + sg + "1.csv").c_str());
		system(("rm " + filepath +  id + "_letsee_all1.csv").c_str());
		system(("rm " + filepath1 + id + "_unique_tcp_allpackets1.csv").c_str());

		// cout<<"No. of SYN packets: "<<uniqueServerIPs<<endl;
		cout<<"File opened successfully: " + sg<<endl<<endl;
	} else {
		cout<<"Unable to open file: " + sg<<endl<<endl;
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
