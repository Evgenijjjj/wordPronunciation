//
//  main.cpp
//  parseWordsPairsToJavaArray
//
//  Created by Evgeny on 24/11/2018.
//  Copyright © 2018 Evgeny. All rights reserved.
//

#include <iostream>
#include <fstream>
#include <string>
using namespace std;

int main () {
    string text;
    string line;
    ifstream myfile ("text.txt");
    if (myfile.is_open())
    {
        while ( myfile.good() )
        {
            getline (myfile,line);
            text += ";" + line;
            //cout << line << endl;
        }
        myfile.close();
    }
    
    else cout << "Unable to open file";
    
    //cout << text << endl;
    
    /*string parsedText = "listOf(WordPair(\"";
    
    for(unsigned i = 0; i < text.size(); i++) {
        if(i != 0 && text[i] == ';') {
            parsedText += "\"), WordPair(\"";
            continue;
        }
        
        if (text[i] == '-') {
            parsedText += "\", \"";
            continue;
        }
        
        if (!isspace(text[i])) {
            parsedText += text[i];
        }
    }
    
    parsedText += "\")";*/
    
    string parsedText;
    bool textFlag = true;
    for(unsigned i = 0; i < text.size(); i++) {
        if(i != 0 && text[i] == ';') {
            parsedText += '\n';
            textFlag = true;
            continue;
        }
        
        if (isspace(text[i])) {
            int p = i;
            while (isspace(text[p])) {
                p++;
            }
            
            if (text[p] == '-') {
                textFlag = false;
                parsedText += '-';
            }
            else {
                textFlag = true;
            }
        }
        
        if (text[i] && textFlag) {
            parsedText += text[i];
        }
    }
    
    /*unsigned i = 0;
    while (i < text.size()) {
        if(i != 0 && text[i] == ';') {
            parsedText += '\n';
            continue;
        }
        
        if (text[i] == '-') {
         //parsedText += "\", \"";
            while (isspace(text[i])) {i--; parsedText = parsedText.substr(0, parsedText.size()-1);}
            parsedText += '-';
            while (text[i-1] != '-') i++;
            while (isspace(text[i])) i++;
         continue;
         }
        
        parsedText += text[i];
    }*/
    
    ofstream out("out1.txt");
    out << parsedText;
    out.close();
    return 0;
}















