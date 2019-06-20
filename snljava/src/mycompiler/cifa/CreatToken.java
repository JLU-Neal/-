package mycompiler.cifa;
import java.io.*;
import java.awt.*;
import java.util.*;

class TokenType {
	int lineshow;
	String Lex;
	String Sem;
}

class ChainNodeType {
	TokenType Token = new TokenType(); // ����
	ChainNodeType nextToken = null; // ָ����һ�����ʵ�ָ��
}

/********************************************************************/
/* �� �� CreatToken */
/* �� �� �ܳ���Ĵ��� */
/* ˵ �� ����һ���࣬�����ܳ��� */
/********************************************************************/
public class CreatToken {
	/* MAXTOKENLENΪ������󳤶ȶ���Ϊ40 */
	int MAXTOKENLEN = 40;

	int l = 0; /* ��¼Դ���򳤶� */
	int char_num = 0; /* ��¼�ı��е��ֽ�λ�� */
	int lineno = 1; /* ��¼�кţ���1��ʼ */
	int Tokennum = 0; /* ��¼token�����ı��� */
	/* �������Ƿ���Сд��������ֻ��Сд������д�д��Ϊ��ʶ����isΪfalse */
	boolean is = true;
	boolean EOF = false; /* EOF��Ϊ�ļ�βʱ,ֵΪtrue */
	boolean Error = false;
	public String tok = null;

	/********************************************************************/
	/* ������ CreatToken */
	/* �� �� ���캯�� */
	/* ˵ �� */
	/********************************************************************/
	public CreatToken(String s) {
		tok = returnTokenlist(getTokenlist(s));
	}

	/************************************************************/
	/* ������ getTokenlist */
	/* �� �� ȡ��Token���к��� */
	/* ˵ �� ������Դ�ļ��ַ��������л�ȡ����Token���� */
	/* ʹ��ȷ���������Զ���DFA,����ֱ��ת�� */
	/* ��ǰ���ַ�,�Ա����ֲ��ò��ʽʶ�� */
	/* �����ʷ�����ʱ��,�����Թ�����������ַ�,���Ӹ��� */
	/************************************************************/
	ChainNodeType getTokenlist(String s) {
		ChainNodeType chainHead = new ChainNodeType(); /* ����ı�ͷ */
		ChainNodeType preNode = chainHead; /* ��ǰ����ǰ����� */
		TokenType currentToken = new TokenType(); /* ��ŵ�ǰ��Token */

		/*
		 * String ss=null; int beg=0; StringTokenizer fenxi=new
		 * StringTokenizer(s,"\n"); while (fenxi.hasMoreTokens()) { String
		 * stok=fenxi.nextToken(); if (beg==0) ss=stok+" "+"\n"; else
		 * ss=ss+stok+" "+"\n"; beg=1; }
		 */
		s = s + " ";
		l = s.length();
		char t[] = s.toCharArray();

		do {
			/*
			 * tokenStringIndex���ڼ�¼��ǰ����ʶ�𵥴ʵĴ�Ԫ�洢�� tokenString�еĵ�ǰ����ʶ���ַ�λ��,��ʼΪ0
			 */
			char tokenString[] = new char[MAXTOKENLEN + 1];
			int tokenStringIndex = 0;

			/* ��ǰ״̬��־state,ʼ�ն�����START��Ϊ��ʼ */
			String state = "START";

			/* tokenString�Ĵ洢��־save,������ǰʶ���ַ��Ƿ����tokenString */
			boolean save;

			is = true;

			/* ��ǰȷ���������Զ���DFA״̬state�������״̬DONE */
			while (!(state.equals("DONE"))) {
				/* ��Դ�����ļ��л�ȡ��һ���ַ�,�������c��Ϊ��ǰ�ַ� */
				char c = getNextChar(t);

				/* ��ǰ��ʶ���ַ��Ĵ洢��־save��ʼΪTRUE */
				save = true;

				/*
				 * ��EOFΪtrue,�����ļ�β,�ַ��洢��־save����Ϊfalse,����洢
				 * ��ǰʶ�𵥴ʷ���ֵcurrentToken����Ϊ�ļ���������ENDFILE
				 */
				if (EOF) {
					state = "DONE";
					save = false;
					currentToken.Lex = "ENDFILE";
				} else if (state.equals("START")) {
					/* ��ǰDFA״̬stateΪ��ʼ״̬START,DFA���ڵ�ǰ���ʿ�ʼλ�� */
					/*
					 * ��ǰ�ַ�cΪ����,��ǰDFA״̬state����Ϊ����״̬INNUM ȷ���������Զ���DFA�����������͵�����
					 */
					if (isdigit(c))
						state = "INNUM";

					/*
					 * ��ǰ�ַ�cΪ��ĸ,��ǰDFA״̬state����Ϊ��ʶ��״̬INID ȷ���������Զ���DFA���ڱ�ʶ�����͵�����
					 */
					else if (isalpha(c))
						state = "INID";

					/*
					 * ��ǰ�ַ�cΪð��,��ǰDFA״̬state����Ϊ��ֵ״̬INASSIGN ȷ���������Զ���DFA���ڸ�ֵ���͵�����
					 */
					else if (c == ':') {
						state = "INASSIGN";
						save = false;
					}

					/* ��ǰ�ַ�cΪ.,��ǰDFA״̬state����Ϊ�����±����״̬ */
					/* INRANGE��ȷ���������Զ���DFA���������±�������͵����� */else if (c == '.') {
						state = "INRANGE";
						save = false;
					}

					/* ��ǰ�ַ�cΪ',��ǰDFA״̬state����Ϊ�ַ���־״̬ */
					/* INCHAR��ȷ���������Զ���DFA�����ַ���־���͵����� */
					else if (c == '\'') {
						save = false;
						state = "INCHAR";
					}

					/*
					 * ��ǰ�ַ�cΪ�հ�(�ո�,�Ʊ��,���з�),�ַ��洢��־save����ΪFALSE
					 * ��ǰ�ַ�Ϊ�ָ���,����Ҫ��������,����洢
					 */
					else if ((c == ' ') || (c == '\t') || (c == '\n')
							|| (c == '\r'))
						save = false;

					/*
					 * ��ǰ�ַ�cΪ������,�ַ��洢��־save����Ϊfalse ��ǰDFA״̬state����Ϊע��״̬INCOMMENT
					 * ȷ���������Զ���DFA����ע����,�����ɵ���,����洢
					 */
					else if (c == '{') {
						save = false;
						state = "INCOMMENT";
					}

					/*
					 * ��ǰ�ַ�cΪ�����ַ�,��ǰDFA״̬state����Ϊ���״̬DONE
					 * ȷ���������Զ���DFA���ڵ��ʵĽ���λ��,���һ�����ദ��
					 */
					else {
						state = "DONE";
						save = false;
						switch (c) {
						/*
						 * ��ǰ�ַ�cΪ"=",��ǰʶ�𵥴ʷ���ֵcurrentToken����Ϊ �Ⱥŵ���EQ
						 */
						case '=':
							currentToken.Lex = "EQ";
							break;

						/*
						 * ��ǰ�ַ�cΪ"<",��ǰʶ�𵥴ʷ���ֵcurrentToken����Ϊ С�ڵ���LT
						 */
						case '<':
							currentToken.Lex = "LT";
							break;

						/*
						 * ��ǰ�ַ�cΪ"+",��ǰʶ�𵥴ʷ���ֵcurrentToken����Ϊ �Ӻŵ���PLUS
						 */
						case '+':
							currentToken.Lex = "PLUS";
							break;

						/*
						 * ��ǰ�ַ�cΪ"-",��ǰʶ�𵥴ʷ���ֵcurrentToken����Ϊ ���ŵ���MINUS
						 */
						case '-':
							currentToken.Lex = "MINUS";
							break;

						/*
						 * ��ǰ�ַ�cΪ"*",��ǰʶ�𵥴ʷ���ֵcurrentToken����Ϊ �˺ŵ���TIMES
						 */
						case '*':
							currentToken.Lex = "TIMES";
							break;

						/*
						 * ��ǰ�ַ�cΪ"/",��ǰʶ�𵥴ʷ���ֵcurrentToken����Ϊ ���ŵ���OVER
						 */
						case '/':
							currentToken.Lex = "OVER";
							break;

						/*
						 * ��ǰ�ַ�cΪ"(",��ǰʶ�𵥴ʷ���ֵcurrentToken����Ϊ �����ŵ���LPAREN
						 */
						case '(':
							currentToken.Lex = "LPAREN";
							break;

						/*
						 * ��ǰ�ַ�cΪ")",��ǰʶ�𵥴ʷ���ֵcurrentToken����Ϊ �����ŵ���RPAREN
						 */
						case ')':
							currentToken.Lex = "RPAREN";
							break;

						/*
						 * ��ǰ�ַ�cΪ";",��ǰʶ�𵥴ʷ���ֵcurrentToken����Ϊ �ֺŵ���SEMI
						 */
						case ';':
							currentToken.Lex = "SEMI";
							break;
						/*
						 * ��ǰ�ַ�cΪ",",��ǰʶ�𵥴ʷ���ֵcurrentToken����Ϊ ���ŵ���COMMA
						 */
						case ',':
							currentToken.Lex = "COMMA";
							break;
						/*
						 * ��ǰ�ַ�cΪ"[",��ǰʶ�𵥴ʷ���ֵcurrentToken����Ϊ �������ŵ���LMIDPAREN
						 */
						case '[':
							currentToken.Lex = "LMIDPAREN";
							break;

						/*
						 * ��ǰ�ַ�cΪ"]",��ǰʶ�𵥴ʷ���ֵcurrentToken����Ϊ �������ŵ���RMIDPAREN
						 */
						case ']':
							currentToken.Lex = "RMIDPAREN";
							break;

						/*
						 * ��ǰ�ַ�cΪ�����ַ�,��ǰʶ�𵥴ʷ���ֵcurrentToken ����Ϊ���󵥴�ERROR
						 */
						default:
							currentToken.Lex = "ERROR";
							Error = true;
							break;
						}
					}
				}
				/********** ��ǰ״̬��Ϊ��ʼ״̬START�Ĵ������ **********/

				/* ��ǰDFA״̬stateΪע��״̬INCOMMENT,DFA����ע��λ�� */
				else if (state.equals("INCOMMENT")) {
					/* ��ǰ�ַ��洢״̬save����ΪFALSE,ע�������ݲ����ɵ���,����洢 */
					save = false;

					/* ��ǰ�ַ�cΪ"}",ע�ͽ���.��ǰDFA״̬state����Ϊ��ʼ״̬START */
					if (c == '}')
						state = "START";
				}

				/*
				 * ��ǰDFA״̬stateΪ��ֵ״̬INASSIGN, ȷ���������Զ���DFA���ڸ�ֵ����λ��
				 */
				else if (state.equals("INASSIGN")) {
					/* ��ǰDFA״̬state����Ϊ���״̬DONE,��ֵ���ʽ��� */
					state = "DONE";
					save = false;

					/*
					 * ��ǰ�ַ�cΪ"=",��ǰʶ�𵥴ʷ���ֵcurrentToken����Ϊ ��ֵ����ASSIGN
					 */
					if (c == '=')
						currentToken.Lex = "ASSIGN";

					/*
					 * ��ǰ�ַ�cΪ�����ַ�,��":"����"=",�������л������л���һ���ַ�
					 * �ַ��洢״̬save����ΪFALSE,��ǰʶ�𵥴ʷ���ֵcurrentToken����Ϊ ERROR
					 */
					else {
						ungetNextChar();
						currentToken.Lex = "ERROR";
						Error = true;
					}
				}
				/*
				 * ��ǰDFA״̬state����Ϊ�����±����״̬INRANGE, ȷ���������Զ���DFA���������±�������͵�����
				 */
				else if (state.equals("INRANGE")) {
					/* ��ǰDFA״̬state����Ϊ���״̬DONE,��ֵ���ʽ��� */
					state = "DONE";
					save = false;

					/*
					 * ��ǰ�ַ�cΪ".",��ǰʶ�𵥴ʷ���ֵcurrentToken����Ϊ �±��UNDERANGE
					 */
					if (c == '.')
						currentToken.Lex = "UNDERANGE";

					/*
					 * ��ǰ�ַ�cΪ�����ַ�,��"."����".",�������л������л���һ���ַ�
					 * �ַ��洢״̬save����ΪFALSE,��ǰʶ�𵥴ʷ���ֵcurrentToken����Ϊ ERROR
					 */
					else {
						ungetNextChar();
						currentToken.Lex = "DOT";
					}
				}

				/* ��ǰDFA״̬stateΪ����״̬INNUM,ȷ���������Զ����������ֵ���λ�� */
				else if (state.equals("INNUM")) {
					/*
					 * ��ǰ�ַ�c��������,���������л�����Դ�л���һ���ַ�
					 * �ַ��洢��־����ΪFALSE,��ǰDFA״̬state����ΪDONE,���ֵ���ʶ����
					 * ��,��ǰʶ�𵥴ʷ���ֵcurrentToken����Ϊ���ֵ���NUM
					 */
					if (!isdigit(c)) {
						ungetNextChar();
						save = false;
						state = "DONE";
						currentToken.Lex = "INTC";
					}
				}
				/* ��ǰDFA״̬stateΪ�ַ���־״̬INCHAR,ȷ�������Զ��������ַ���־״̬ */
				else if (state.equals("INCHAR")) {
					if (isalpha(c)) {
						char c1 = getNextChar(t);
						if (c1 == '\'') {
							save = true;
							state = "DONE";
							currentToken.Lex = "ID";
						} else {
							ungetNextChar();
							ungetNextChar();
							state = "DONE";
							currentToken.Lex = "ERROR";
							Error = true;
						}
					} else {
						ungetNextChar();
						state = "DONE";
						currentToken.Lex = "ERROR";
						Error = true;
					}
				}
				/*
				 * ��ǰDFA״̬stateΪ��ʶ��״̬INID, ȷ���������Զ���DFA���ڱ�ʶ������λ��
				 */
				else if (state.equals("INID")) {
					/*
					 * ��ǰ�ַ�c������ĸ,���������л�����Դ�л���һ���ַ�
					 * �ַ��洢��־����ΪFALSE,��ǰDFA״̬state����ΪDONE,��ʶ������ʶ��
					 * ���,��ǰʶ�𵥴ʷ���ֵcurrentToken����Ϊ��ʶ������ID
					 */
					if ((!isalpha(c)) && (!isdigit(c))) {
						ungetNextChar();
						save = false;
						state = "DONE";
						currentToken.Lex = "ID";
					}
				}
				/* ��ǰDFA״̬stateΪ���״̬DONE,ȷ���������Զ���DFA���ڵ��ʽ���λ�� */
				else if (state.equals("DONE")) {
				}
				/* ��ǰDFA״̬stateΪ����״̬,���������Ӧ���� */
				else {
					/* ��ǰDFA״̬state����Ϊ���״̬DONE ��ǰʶ�𵥴ʷ���ֵcurrentToken����Ϊ���󵥴�ERROR */
					Error = true;
					state = "DONE";
					currentToken.Lex = "ERROR";
				}
				/*************** �����жϴ������ *******************/

				/*
				 * ��ǰ�ַ��洢״̬saveΪTRUE,�ҵ�ǰ��ʶ�𵥴��Ѿ�ʶ�𲿷�δ��������
				 * ��󳤶�,����ǰ�ַ�cд�뵱ǰ��ʶ�𵥴ʴ�Ԫ�洢��tokenString
				 */
				if ((save) && (tokenStringIndex <= MAXTOKENLEN)) {
					tokenString[tokenStringIndex] = c;
					tokenStringIndex = tokenStringIndex + 1;
				}
				if (state.equals("DONE")) {
					/* ��ǰDFA״̬stateΪ���״̬DONE,����ʶ�����,����ת��Ϊ�ַ��� */
					String st = (new String(tokenString)).trim(); /* ȥ��ǰ��ո� */

					/* ��ǰ����currentTokenΪ��ʶ����������,�鿴���Ƿ�Ϊ�����ֵ��� */
					if (currentToken.Lex.equals("ID")) {
						if (is) /* �������ȫ��Сд�����п����Ǳ����� */
							currentToken.Lex = reservedLookup(st);
						if (currentToken.Lex.equals("ID"))
							currentToken.Sem = st;
						else
							currentToken.Sem = " ";
					} else if (currentToken.Lex.equals("INTC"))
						currentToken.Sem = st;
					else
						currentToken.Sem = " ";
				}
			}
			/**************** ѭ��������� ********************/
			/* ���к���Ϣ����Token */
			currentToken.lineshow = lineno;

			Tokennum++; /* Token����Ŀ��1 */

			copy(preNode, currentToken);
			preNode.nextToken = new ChainNodeType();
			preNode = preNode.nextToken;
		}
		/*
		 * ֱ���������ʾ�ļ�������Token:ENDFILE��˵�����������е�Token �������������У�ѭ������
		 */
		while (!(currentToken.Lex.equals("ENDFILE")));
		return chainHead;
	}

	/*******************************************************************/
	/* ������ getNextChar */
	/* �� �� ȡ����һ�ǿ��ַ����� */
	/* ˵ �� ��ȡһ���ֽڵ�����,�����ļ�βʱ,EOFΪtrue */
	/*******************************************************************/
	char getNextChar(char t[]) {
		char a = ' ';
		if (char_num < l) {
			if (t[char_num] == '\n')
				lineno++;
			a = t[char_num];
			char_num++;
		} else
			EOF = true;
		return a;
	}

	/********************************************************/
	/* ������ ungetNextChar */
	/* �� �� �ַ����˺��� */
	/* ˵ �� ����һ���ֽڵ����� */
	/********************************************************/
	void ungetNextChar() {
		/* ���EOFΪfalse,���Ǵ���Դ�ļ�ĩβ,����һ���ֽ� */
		if (!EOF)
			char_num--;
	}

	/****************************************************/
	/* ������ isdigit */
	/* �� �� ������c�ǲ������� */
	/* ˵ �� */
	/****************************************************/
	boolean isdigit(char c) {
		if ((c == '0') || (c == '1') || (c == '2') || (c == '3') || (c == '4')
				|| (c == '5') || (c == '6') || (c == '7') || (c == '8')
				|| (c == '9'))
			return true;
		else
			return false;
	}

	/****************************************************/
	/* ������ isalpha */
	/* �� �� ������c�ǲ�����ĸ */
	/* ˵ �� */
	/****************************************************/
	boolean isalpha(char c) {
		if ((c == 'a') || (c == 'b') || (c == 'c') || (c == 'd') || (c == 'e')
				|| (c == 'f') || (c == 'g') || (c == 'h') || (c == 'i')
				|| (c == 'j') || (c == 'k') || (c == 'l') || (c == 'm')
				|| (c == 'n') || (c == 'o') || (c == 'p') || (c == 'q')
				|| (c == 'r') || (c == 's') || (c == 't') || (c == 'u')
				|| (c == 'v') || (c == 'w') || (c == 'x') || (c == 'y')
				|| (c == 'z'))
			return true;
		else if ((c == 'A') || (c == 'B') || (c == 'C') || (c == 'D')
				|| (c == 'E') || (c == 'F') || (c == 'G') || (c == 'H')
				|| (c == 'I') || (c == 'J') || (c == 'K') || (c == 'L')
				|| (c == 'M') || (c == 'N') || (c == 'O') || (c == 'P')
				|| (c == 'Q') || (c == 'R') || (c == 'S') || (c == 'T')
				|| (c == 'U') || (c == 'V') || (c == 'W') || (c == 'X')
				|| (c == 'Y') || (c == 'Z')) {
			is = false;
			return true;
		} else
			return false;
	}

	/**************************************************************/
	/* ������ reservedLookup */
	/* �� �� �����ֲ��Һ��� */
	/* ˵ �� ʹ�����Բ���,�鿴һ����ʶ���Ƿ��Ǳ����� */
	/* ��ʶ������ڱ����ֱ����򷵻���Ӧ����,���򷵻ص���ID */
	/**************************************************************/
	String reservedLookup(String s) {
		/* �ַ���s�뱣���ֱ���ĳһ����ƥ��,�������ض�Ӧ�����ֵ��� */
		if (s.equals("program"))
			return "PROGRAM";
		else if (s.equals("type"))
			return "TYPE";
		else if (s.equals("var"))
			return "VAR";
		else if (s.equals("procedure"))
			return "PROCEDURE";
		else if (s.equals("begin"))
			return "BEGIN";
		else if (s.equals("end"))
			return "END";
		else if (s.equals("array"))
			return "ARRAY";
		else if (s.equals("of"))
			return "OF";
		else if (s.equals("record"))
			return "RECORD";
		else if (s.equals("if"))
			return "IF";
		else if (s.equals("then"))
			return "THEN";
		else if (s.equals("else"))
			return "ELSE";
		else if (s.equals("read"))
			return "READ";
		else if (s.equals("write"))
			return "WRITE";
		else if (s.equals("return"))
			return "RETURN";
		else if (s.equals("integer"))
			return "INTEGER";
		else if (s.equals("fi"))
			return "FI";
		else if (s.equals("while"))
			return "WHILE";
		else if (s.equals("do"))
			return "DO";
		else if (s.equals("endwh"))
			return "ENDWH";
		else if (s.equals("char"))
			return "CHAR";
		else
			/* �ַ���sδ�ڱ����ֱ����ҵ�,�������ر�ʶ������ID */
			return "ID";
	}

	/*****************************************************************/
	/* ������ copy */
	/* �� �� ��b�е���Ϣ������a.Token�С� */
	/* ˵ �� */
	/*****************************************************************/
	void copy(ChainNodeType a, TokenType b) {
		a.Token.lineshow = b.lineshow;
		a.Token.Lex = b.Lex;
		a.Token.Sem = b.Sem;
	}

	/*****************************************************************/
	/* ������ returnTokenlist */
	/* �� �� ��Token�����������ʾ���ļ��С� */
	/* ˵ �� ������ʾ�ʷ�������� */
	/*****************************************************************/
	String returnTokenlist(ChainNodeType n) {
		String a = " ";
		ChainNodeType node = n;
		TokenType token = n.Token;
		for (int m = 1; m <= Tokennum; m++) {
			a = a + String.valueOf(token.lineshow) + ":" + token.Lex + ",";

			if (token.Sem == null)
				a = a + " ";
			else
				a = a + token.Sem; /* ���Sem */

			a = a + "\n";
			node = node.nextToken;
			token = node.Token;
		}
		return a;
	}
}
