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

int maxTimeDuration(map<pair<string, string>, pair<int, int>> res){
	int maxd = INT_MIN;
	map<pair<string, string>, pair<int, int>>::iterator itr;
	for(itr = res.begin();itr!=res.end();itr++){
		int time = (itr->second).second - (itr->second).first;
		if(time>maxd){maxd = time;}
	}
	return maxd;
}

double meanTimeDuration(map<pair<string, string>, pair<int, int>> res){
	double sum = 0;
	int count = 0;
	map<pair<string, string>, pair<int, int>>::iterator itr;
	for(itr = res.begin();itr!=res.end();itr++){
		// int time = (itr->second).second - (itr->second).first;
		// if(time>maxd){maxd = time;}
		sum += (itr->second).second - (itr->second).first;
		count++;
	}
	return (double)(sum)/(count);
}

int isDestinationPort21(string s){
	string s1 = s;
	string s2 = ">  21";
	if (s1.find(s2) != std::string::npos){
		return 1;
	}
	return -1;
}

void connectionDurationBetweenConnections(string id, map<pair<string, string>, pair<double, double>> res)
{
	map<pair<string, string>, pair<double, double>>::iterator itr;
	vector<double> store;
	cout << "yo man!!!" <<endl;
	for(itr = res.begin();itr!=res.end();itr++){
		cout << (itr->second).first << endl;
		store.push_back((itr)->second.first);
	}
	sort(store.begin(), store.end());
	vector<double> diff;
	for (int i = 1; i < store.size(); i++)
	{
		cout<< store[i] << " "<<store[i-1] << " " << store[i]-store[i-1] << endl;
		diff.push_back(store[i]-store[i-1]);
	}
	
	ofstream cdfFile;
	cdfFile.open(id + "_connectionDurationBetweenConnections.csv");
	cdfFile << "\"Probability\""<<endl;
	for(int i=0;i<diff.size();i++){
		// answertocdf[i] /= tempp;
		cdfFile <<"\""<<(diff[i])<<"\""<<endl;
	}
}


void connectionDuration(string filename){
	map<pair<string, string>, pair<int, int>> res;
	map<pair<string, string>, pair<double, double>> resd;
	// res.insert({make_pair("0","0"), make_pair(0,0)});
	string id = filename.substr(0,filename.length()-4);
	ifstream myfile(filename);
	ofstream cdfFile;

	if(myfile.is_open() && filename.find("_letsee_all") != std::string::npos){
		// cout<<"OPENED " + filename<<endl;
		cdfFile.open(id + "_cdf.csv");
		string l;
		while(getline(myfile,l)){
			vector<string> line = giveTokens(l);
			string sourceIP = sourceip(l);
			string destIP = destip(l);
			int its_time = stoi(line[1].substr(1,line[1].length()-2));
			double its_timed = stod(line[1].substr(1,line[1].length()-2));
			// cout<<sourceIP<< " ? " <<destIP<<endl;
			pair<string, string> pr1 = make_pair(sourceIP, destIP);
			pair<string, string> pr2 = make_pair(destIP, sourceIP);
			map<pair<string, string>, pair<int, int>>::iterator it = res.find(pr1);
			map<pair<string, string>, pair<int, int>>::iterator it2 = res.find(pr2);
			
			map<pair<string, string>, pair<double, double>>::iterator itd = resd.find(pr1);
			map<pair<string, string>, pair<double, double>>::iterator itd2 = resd.find(pr2);
			
			if(it==res.end() && synOrFinAckOrRst(line[6])==-1){
				res.insert({pr1, make_pair(its_time,0)});
				resd.insert({pr1, make_pair(its_timed,0)});
			} else if((it2!=res.end()) && synOrFinAckOrRst(line[6])>=0){
				it2->second.second = its_time;// = make_pair(it->second.first, its_time);
				itd2->second.second = its_time;// = make_pair(it->second.first, its_time);
			} else if((it!=res.end()) && synOrFinAckOrRst(line[6])>=0){
				it->second.second = its_time;// = make_pair(it->second.first, its_time);
				itd->second.second = its_time;// = make_pair(it->second.first, its_time);
			}
		}
		connectionDurationBetweenConnections(id, resd);
		// printSpecialMaps(res);
		// miniController(id, res); ---------------------------------
		int max_duration = maxTimeDuration(res);
		double tempp = 0;

		vector<double> answertocdf(max_duration,0);
		
		map<pair<string, string>, pair<int, int>>::iterator itr;
		
		for(int i=0;i<=max_duration;i++){
			for(itr = res.begin();itr!=res.end();itr++){
				int time = (itr->second).second - (itr->second).first;
				if(time < i){
					answertocdf[i] += 1;
				}
			}
		}
		tempp = answertocdf[answertocdf.size()-1];
		cdfFile << "\"Connection Duration\"," << "\"Probability\""<<endl;
		for(int i=0;i<answertocdf.size();i++){
			// answertocdf[i] /= tempp;
			// cout<<i<<" -> "<<(answertocdf[i])/tempp<<endl; 
			cdfFile << "\""<<i<<"\""<<",\""<<(answertocdf[i])/tempp<<"\""<<endl;
		}
		cout<<"Mean Time Duration for " + id<<": "<<meanTimeDuration(res)<<endl;
		//-----------------------------------------------------------

	} else {
		cout<<"File " + filename + " not opened correctly."<<endl;
	}

}

void sentAndRecData(string filename){
	map<pair<string, string>, pair<int, int>> res;
	// res.insert({make_pair("0","0"), make_pair(0,0)});
	string id = filename.substr(0,filename.length()-4);
	ifstream myfile(filename);
	ofstream outputq5;

	if(myfile.is_open() && filename.find("_unique_tcp_allpackets") != std::string::npos){
		outputq5.open(id + "_q5.csv");
		string l;
		getline(myfile,l);

		while(getline(myfile,l)){
			vector<string> line = giveTokens(l);
			string sourceIP = sourceip(l);
			string destIP = destip(l);

			// cout<<stoi(line[5].substr(1,line[5].length()-2))<<endl;
			int bytelength = stoi(line[5].substr(1,line[5].length()-2));

			pair<string, string> pr1 = make_pair(sourceIP, destIP);
			pair<string, string> pr2 = make_pair(destIP, sourceIP);
			map<pair<string, string>, pair<int, int>>::iterator it1 = res.find(pr1);
			map<pair<string, string>, pair<int, int>>::iterator it2 = res.find(pr2);

			bool isRequestToFTP = (line[6].find("Request") != std::string::npos);
			bool isfromClient = (isDestinationPort21(line[6])==1) || isRequestToFTP;

			if(isfromClient){
				if(it1 == res.end() && it2 ==res.end()){
					res.insert({pr1, make_pair(bytelength,0)});
				} else if(it1!=res.end()){
					it1->second.first += bytelength;
				}
			} else if(!isfromClient){
				if(it2 == res.end() && it1 == res.end()){
					res.insert({pr2, make_pair(0,bytelength)});
				} else if(it2 != res.end()){
					it2->second.second += bytelength;
				} 
			}
		}
		outputq5<<"\""<<"Client IP"<< "\"," <<"Server IP"<< "\"," <<"Data Sent"<< "\"," <<"\"Data Recieved\""<<endl;
		map<pair<string, string>, pair<int, int>>::iterator itr;
		for(itr = res.begin();itr!=res.end();itr++){
			outputq5<<(itr->first).first << "," << (itr->first).second << ",\"" << (itr->second).first << "\",\"" << (itr->second).second<<"\""<<endl;
		}
		// printSpecialMaps(res);
		outputq5.close();
	} else {
		cout<<"Cannot open "<<filename<<endl;
	}
}

int main(int argc, char const *argv[])
{
	string path_header = "./outputs/q4/";
	string path_header_q5 = "./outputs/q5/";
	vector<string> s = {{"lbnl.anon-ftp.03-01-11"},{"lbnl.anon-ftp.03-01-14"},{"lbnl.anon-ftp.03-01-18"}}; 
	for(int i=0;i<s.size();i++){connectionDuration(path_header + s[i] + "_letsee_all.csv");}
	for(int i=0;i<s.size();i++){sentAndRecData(path_header_q5 + s[i] + "_unique_tcp_allpackets.csv");}
	return 0;
}
