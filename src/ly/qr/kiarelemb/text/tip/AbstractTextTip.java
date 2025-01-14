package ly.qr.kiarelemb.text.tip;

import ly.qr.kiarelemb.data.Keys;
import ly.qr.kiarelemb.data.TipData;
import ly.qr.kiarelemb.data.TypingData;
import ly.qr.kiarelemb.text.tip.data.TextStyleManager;
import ly.qr.kiarelemb.text.tip.data.TipCharStyleData;
import ly.qr.kiarelemb.text.tip.data.TipPhraseStyleData;
import method.qr.kiarelemb.utils.QRArrayUtils;
import method.qr.kiarelemb.utils.QRFileUtils;
import method.qr.kiarelemb.utils.QRMathUtils;
import method.qr.kiarelemb.utils.QRStringUtils;
import swing.qr.kiarelemb.component.QRComponentUtils;
import swing.qr.kiarelemb.inter.QRActionRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Kiarelemb QR
 * @program: 揽月跟打器
 * @description:
 * @create 2023-02-03 12:31
 **/
public abstract class AbstractTextTip {
    protected AbstractTextTip.SubscriptInstance[] subscriptInstances;
    protected HashMap<String, String> wordCode;
    protected ArrayList<String> symbolEntry;
    protected ArrayList<HashMap<String, String>> wordsCodeList;
    protected String selection = null;
    protected int codeLength = 4;
    protected boolean loaded = false;
    protected boolean three42 = false;
    protected String filePath;

    private final ArrayList<QRActionRegister> loadActions = new ArrayList<>();
    private final ArrayList<QRActionRegister> disloadActions = new ArrayList<>();
    public static AbstractTextTip TEXT_TIP;

    static {
        if (Keys.boolValue(Keys.TEXT_TIP_ENHANCE)) {
            TEXT_TIP = new TextTipEnhance();
        } else {
            TEXT_TIP = new TextTip();
        }
    }

    public AbstractTextTip() {
    }

    public abstract void changeColorTip(String text);

    public final void load() {
        if (Keys.boolValue(Keys.TEXT_TIP_ENABLE)) {
            String filePath = Keys.strValue(Keys.TEXT_TIP_FILE_PATH);
            if (QRFileUtils.fileExists(filePath)) {
                if (this.filePath == null || !this.filePath.equals(filePath)) {
                    this.filePath = filePath;
                    this.selection = Keys.strValue(Keys.TEXT_TIP_SELECTION);
                    int value = Keys.intValue(Keys.TEXT_TIP_CODE_LENGTH);
                    if (value > 0) {
                        this.codeLength = 3;
                        this.three42 = value == 2;
                    }
                    loadFile(filePath);
                    QRComponentUtils.runActions(this.loadActions);
                }
            }
        }
    }

    public final void release() {
        if (this.loaded) {
            this.subscriptInstances = null;
            this.wordCode.clear();
            this.wordsCodeList.clear();
            this.symbolEntry.clear();
            this.filePath = null;
            this.loaded = false;
            System.gc();
        }
        if (!Keys.boolValue(Keys.TEXT_TIP_ENABLE)) {
            QRComponentUtils.runActions(this.disloadActions);
        }
    }

    public final void addLoadAction(QRActionRegister ar) {
        this.loadActions.add(ar);
    }

    public final void addDisloadAction(QRActionRegister ar) {
        this.disloadActions.add(ar);
    }

    public final TipData GetShortestCode(String text) {
        return GetShortestCode(text, 0);
    }

    public final TipData GetShortestCode(String text, int foreI) {
        ArrayList<TipPhraseStyleData> tpsd = new ArrayList<>();
        ArrayList<TipCharStyleData> tcsd = new ArrayList<>();
        ArrayList<Integer> indexes = new ArrayList<>();
        int maxLen = this.codeLength + 1;
        try {
            changeColorTip(text);
        } catch (Exception ignore) {
        }
        StringBuilder allCodes = new StringBuilder();
        int foreType = -1;
        int singleLength = 0;
        boolean foreBold = false;
        if (this.subscriptInstances == null) {
            return null;
        }
        int first = 0;
        int multi = 0;

        int singleCounts = 0;
        int phraseNum = 0;
        int allTypeNum = 0;
        int oneFirst = 0;
        int oneMulti = 0;
        int twoFirst = 0;
        int twoMulti = 0;
        int threeFirst = 0;
        int threeMulti = 0;
        int fourFirst = 0;
        int fourMulti = 0;

        int totalCounts;
        int leftCounts = 0;
        int rightCounts = 0;
        int spaceCounts = 0;
        String regex = this.selection.startsWith("_") ? this.selection : "_".concat(this.selection);

        for (int i = 0, l = this.subscriptInstances.length; i < l; i++) {
            //词组的输出
            if (this.subscriptInstances[i].isUseSign()) {
                //先取出有词组的每个单字
                for (int x = i; x <= this.subscriptInstances[i].getNext(); ) {
                    String word = this.subscriptInstances[x].getWord();
                    String code = this.subscriptInstances[x].getWordCode();
                    final int codeLen = code.length();
                    if (code.endsWith("_") && ((three42 && codeLen == 3) || (!three42 && codeLen == maxLen)) && i != this.subscriptInstances.length - 1) {
                        code = code.substring(0, this.codeLength);
                    }
                    tcsd.add(new TipCharStyleData(x, word, code, TextStyleManager.STYLE_TYPE[0], TextStyleManager.BOLD_FALSE));
                    x += word.length();
                    singleLength += code.length();
                }
                i = this.subscriptInstances[i].getNext();
                String words = this.subscriptInstances[i].getShortCodePreInfo().getWords();
                String code = this.subscriptInstances[i].getShortCodePreInfo().getWordsCode();
                int codeLen = code.length();
                int type = getType(code);
//                if (code.endsWith("_") && codeLen == maxLen && i != this.subscriptInstances.length - 1) {
                if (code.endsWith("_") && ((three42 && codeLen == 3) || (!three42 && codeLen == maxLen)) && i != this.subscriptInstances.length - 1) {
                    code = code.substring(0, this.codeLength);
                }
                phraseNum++;
                char c = code.charAt(codeLen - 1);
                if (c != '_' && regex.indexOf(c) != -1) {
                    int clen = codeLen - 1;
                    switch (clen) {
                        case 1:
                            oneMulti++;
                            multi++;
                            break;
                        case 2:
                            twoMulti++;
                            multi++;
                            break;
                        case 3:
                            threeMulti++;
                            multi++;
                            break;
                        case 4:
                            fourMulti++;
                            multi++;
                            break;
                    }
                } else {
                    int clen = c == '_' ? codeLen - 1 : codeLen;
                    switch (clen) {
                        case 1:
                            oneFirst++;
                            first++;
                            break;
                        case 2:
                            twoFirst++;
                            first++;
                            break;
                        case 3:
                            threeFirst++;
                            first++;
                            break;
                        case 4:
                            fourFirst++;
                            first++;
                            break;
                    }
                }
                boolean bold = (type != 0) && (type == foreType) && !foreBold;
                tpsd.add(new TipPhraseStyleData(foreI, words, code, type, bold, true));
                indexes.add(foreI);
                for (int tmp = foreI; tmp < i; tmp++) {
                    tpsd.add(null);
                }
                foreType = type;
                foreBold = bold;
                allCodes.append(code);
                int wordsLen = words.length();
                foreI += wordsLen;
            } else {
                //单字的输出
                String word = this.subscriptInstances[i].getWord();
                String code = this.subscriptInstances[i].getWordCode();
                int codeLen = code.length();
                int type = getType(code);
                if (code.endsWith("_") && ((three42 && codeLen == 3) || (!three42 && codeLen == maxLen)) && i != this.subscriptInstances.length - 1) {
                    code = code.substring(0, this.codeLength);
                }
                singleCounts++;
                char c = code.charAt(codeLen - 1);
                if (c != '_' && regex.indexOf(c) != -1) {
                    int clen = codeLen - 1;
                    switch (clen) {
                        case 1:
                            oneMulti++;
                            multi++;
                            break;
                        case 2:
                            twoMulti++;
                            multi++;
                            break;
                        case 3:
                            threeMulti++;
                            multi++;
                            break;
                        case 4:
                            fourMulti++;
                            multi++;
                            break;
                    }
                } else {
                    int clen = c == '_' ? codeLen - 1 : codeLen;
                    switch (clen) {
                        case 1:
                            oneFirst++;
                            first++;
                            break;
                        case 2:
                            twoFirst++;
                            first++;
                            break;
                        case 3:
                            threeFirst++;
                            first++;
                            break;
                        case 4:
                            fourFirst++;
                            first++;
                            break;
                    }
                }
                tcsd.add(new TipCharStyleData(foreI, word, code, type, TextStyleManager.BOLD_FALSE));
                tpsd.add(new TipPhraseStyleData(foreI, word, code, type, TextStyleManager.BOLD_FALSE, false));
                indexes.add(foreI);
                foreType = TextStyleManager.STYLE_TYPE[0];
                foreBold = false;
                allCodes.append(code);
                foreI += word.length();
                singleLength += code.length();
            }
            allTypeNum++;
        }
        final String codes = allCodes.toString();
        char[] codeChars = codes.toCharArray();
        for (char c : codeChars) {
            if (TypingData.LEFT.indexOf(c) != -1) {
                leftCounts++;
            } else if (TypingData.RIGHT.indexOf(c) != -1) {
                rightCounts++;
            } else if (c == '_') {
                spaceCounts++;
            }
        }
        totalCounts = codeChars.length;
        double phraseTypeCounts = 100 * QRMathUtils.doubleFormat((phraseNum + 0.0) / allTypeNum, 4);
        TipData.StandardData data = new TipData.StandardData(first, multi, singleCounts, phraseTypeCounts, oneFirst, oneMulti, twoFirst, twoMulti, threeFirst, threeMulti, fourFirst, fourMulti, totalCounts, leftCounts, rightCounts, spaceCounts);
        return new TipData(tcsd, tpsd, codes, QRArrayUtils.listToArr(indexes), singleLength, data);
    }

    private boolean is42AndCode2(String code) {
        if (!three42) {
            return false;
        }
        boolean codeEndsWithBlank = code.endsWith("_");
        if (codeEndsWithBlank) {
            return code.length() == 3;
        } else {
            return code.length() == 2;
        }
    }

    public String selection() {
        return selection;
    }

    public int codeLength() {
        return codeLength;
    }

    public boolean three42() {
        return three42;
    }

    public int getType(String codeTemp) {
        int lengthTemp = codeTemp.length();
        //获取编码最后一个字符
        String lastStr = QRStringUtils.lastChar(codeTemp);
        //非首选标记
        boolean first;
        if (this.selection.contains(lastStr)) {
            //判断最后一字符是否为多重
            first = false;
            lengthTemp -= 1;
        } else if ("_".equals(lastStr)) {
            first = true;
            lengthTemp -= 1;
        } else {
            first = true;
        }
        if (lengthTemp < 2) {
            //0单 1全 2次全 3三简 4 次三简 5二简  6次二简
            if (first) {
                return AbstractTextTip.Type.yj.code;
            } else {
                return AbstractTextTip.Type.cyj.code;
            }
        } else if (lengthTemp < 3) {
            //0单 1全 2次全 3三简 4 次三简 5二简  6次二简
            if (first) {
                return AbstractTextTip.Type.ej.code;
            } else {
                return AbstractTextTip.Type.cej.code;
            }
        } else if (lengthTemp < 4) {
            if (first) {
                return AbstractTextTip.Type.sj.code;
            } else {
                return AbstractTextTip.Type.csj.code;
            }
        } else {
            if (first) {
                return AbstractTextTip.Type.q.code;
            } else {
                return AbstractTextTip.Type.cq.code;
            }
        }
    }

    private void loadFile(String tipFilePath) {
        String topSymbol = "，。";
        this.wordsCodeList = new ArrayList<>(20);
        this.wordCode = new HashMap<>();
        this.symbolEntry = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            HashMap<String, String> moreTipHash = new HashMap<>();
            this.wordsCodeList.add(moreTipHash);
        }
        QRFileUtils.fileReaderWithUtf8(tipFilePath, "\t", ((lineText, split) -> {
            if (split.length == 2) {
                final String ch = split[0];
                final String bm = split[1];
                String temp;
                int chLength = QRStringUtils.getActualChineseLength(split[0]);
                int length = bm.length();
                temp = bm.substring(length - 1);
                if ("_".equals(temp) || this.selection.contains(temp)) {
                    length -= 1;
                }
                int i = -1;
                if (chLength == 1) {
                    if (this.wordCode.containsKey(ch)) {
                        if (this.wordCode.get(ch).length() > length) {
                            this.wordCode.put(ch, bm);
                        }
                    } else {
                        this.wordCode.put(ch, bm);
                    }
                } else if (chLength >= 2) {
                    final int thisLen = chLength - 2;
                    final int size = this.wordsCodeList.size();
                    if (size <= thisLen) {
                        for (int j = size; j <= thisLen; j++) {
                            HashMap<String, String> moretiphash = new HashMap<>();
                            this.wordsCodeList.add(moretiphash);
                        }
                    }
                    i = thisLen;
                }
                if (i != -1) {
                    if (this.wordsCodeList.get(i).containsKey(split[0])) {
                        if (this.wordsCodeList.get(i).get(ch).length() > length) {
                            this.wordsCodeList.get(i).put(ch, bm);
                        }
                    } else {
                        this.wordsCodeList.get(i).put(ch, bm);
                    }
                }
                if (topSymbol.contains(ch.substring(0, 1))) {
                    this.symbolEntry.add(ch);
                }
            }
        }));
        this.loaded = true;
    }

    public boolean loaded() {
        return this.loaded;
    }

    static class SubscriptInstance {
        private final HashMap<Integer, PreInfo> preInfoMap;
        private int next;//下一跳
        private String word;
        private String wordCode;
        //0单 1全 2次全 3三简 4 次三简 5二简  6次二简
        private int type;
        private boolean useSign;
        private boolean useWordSign;
        private int codeLengthTemp;

        static class PreInfo {
            //同长度不同上跳表，用于动态词提
            private final HashMap<Integer, Integer> preAndType = new HashMap<>();
            private final String wordsCode;
            private final String words;

            PreInfo(int pre, String words, String wordsCode, int type) {
                this.preAndType.put(pre, type);
                this.words = words;
                this.wordsCode = wordsCode;
            }

            public String getWordsCode() {
                return this.wordsCode;
            }

            public String getWords() {
                return this.words;
            }

            public HashMap<Integer, Integer> getPre() {
                return this.preAndType;
            }

            public int getMinPre() {
                List<Integer> list = new ArrayList<>(this.preAndType.keySet());
                Collections.sort(list);
                return list.get(0);
            }

            public int getType(int pre) {
                return this.preAndType.get(pre);
            }

            public void addPre(int pre, int type) {
                this.preAndType.put(pre, type);
            }
        }

        public SubscriptInstance(int i) {
            this.next = i;
            this.preInfoMap = new HashMap<>();
            this.wordCode = "";
            this.word = "";
            this.useSign = false;
            this.useWordSign = false;
            this.codeLengthTemp = 0;
        }

        public SubscriptInstance(int i, String word, String wordCode) {
            this(i);
            this.wordCode = wordCode;
            this.word = word;
        }

        public void addPre(int length, int pre, String words, String wordsCode, int type) {
            if (!this.preInfoMap.containsKey(length)) {
                this.preInfoMap.put(length, new PreInfo(pre, words, wordsCode, type));
            } else {
                this.preInfoMap.get(length).addPre(pre, type);
            }
        }

        public PreInfo getShortCodePreInfo() {
            return this.preInfoMap.get(this.codeLengthTemp);
        }

        public boolean isNotUseWordSign() {
            return !this.useWordSign;
        }

        public void setUseWordSign(boolean useWordSign) {
            this.useWordSign = useWordSign;
        }

        public String getWordCode() {
            return this.wordCode;
        }

        public String getWord() {
            return this.word;
        }

        public int getNext() {
            return this.next;
        }

        public void setNext(int next) {
            this.next = next;
        }

        public int getCodeLengthTemp() {
            return this.codeLengthTemp;
        }

        public void setCodeLengthTemp(int codeLengthTemp) {
            this.codeLengthTemp = codeLengthTemp;
        }

        public HashMap<Integer, PreInfo> getPreInfoMap() {
            return this.preInfoMap;
        }

        public boolean isUseSign() {
            return this.useSign;
        }

        public void setUseSign(boolean useSign) {
            this.useSign = useSign;
        }

        public int getType() {
            return this.type;
        }

        public void setType(int type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return this.next + '-' + this.word + '-' + this.wordCode;
        }
    }

    enum Type {
        //1全 2次全 3三简 4 次三简 5二简  6次二简 7一简 8次一简
        q(1), cq(2), sj(3), csj(4), ej(5), cej(6), yj(7), cyj(8);
        public final int code;

        Type(int code) {
            this.code = code;
        }
    }
}