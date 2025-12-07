from flask import Flask, request, jsonify
from konlpy.tag import Okt
from collections import Counter
import random

app = Flask(__name__)
okt = Okt()

# 1. 불용어 (의미 없는 단어 + 악기점 공통 단어 제거)
STOPWORDS = {
    '정말', '진짜', '너무', '아주', '완전', '그냥', '많이', '좀', 
    '것', '곳', '수', '거', '저', '이', '그', '때', '제', '더', '안',
    '나', '우리', '저희', '정도', '점', '해', '등', '분', '여기',
    
    # ✅ 악기점 공통 단어 (정보가치가 낮으므로 제거)
    '악기', '기타', '매장', '가게', '방문', '구경', '구매', '사람', '생각',
    '부분', '관련', '가기', '오다', '가다', '해주다', '있다', '없다', '이다.'
}

# 2. 유의어 통합 맵 (비슷한 표현을 하나의 표준 태그로 통일)
KEYWORD_MAP = {
    # 가격 관련
    '비싸다': '가격대 있음', '비쌈': '가격대 있음',
    '저렴하다': '가격 합리적', '저렴함': '가격 합리적', '싸다': '가격 합리적', 
    '합리적': '가격 합리적', '착하다': '가격 합리적', '가성비': '가성비 좋음',
    
    # 친절 관련
    '친절하다': '사장님 친절함', '친절함': '사장님 친절함', 
    '상냥하다': '사장님 친절함', '좋다': '서비스 좋음', '좋음': '서비스 좋음',
    '무뚝뚝하다': '사장님 무뚝뚝함', '무뚝뚝함': '사장님 무뚝뚝함',
    
    # 실력/수리 관련
    '꼼꼼하다': '수리 꼼꼼함', '꼼꼼함': '수리 꼼꼼함', '잘하다': '실력 좋음',
    '전문': '전문성 있음', '전문적': '전문성 있음', '지식': '전문 지식',
    '셋업': '셋업 잘함', '수리': '수리 전문', '줄': '스트링 교체',
    
    # 시설 관련
    '주차': '주차 공간', '넓다': '매장 넓음', '쾌적하다': '매장 쾌적함',
    '다양하다': '종류 다양함', '다양함': '종류 다양함', '많다': '재고 많음'
}

def normalize_word(word):
    """단어를 표준 태그로 변환 (매핑 테이블 활용)"""
    # 1. 매핑 테이블에 있으면 변환
    if word in KEYWORD_MAP:
        return KEYWORD_MAP[word]
    
    # 2. 매핑엔 없지만 형용사 어미 변환 (~하다 -> ~함)
    if word.endswith("하다") and len(word) > 2:
        return word[:-2] + "함"
    
    return word

def extract_phrases(text):
    """형태소 분석 로직"""
    pos_tags = okt.pos(text, stem=True)
    phrases = []
    
    for i in range(len(pos_tags)):
        curr_word, curr_tag = pos_tags[i]
        
        # 불용어 1차 필터링
        if curr_word in STOPWORDS: continue

        # --- 단독 키워드 추출 ---
        # 명사(Noun)이거나 형용사(Adjective)인 경우
        if curr_tag in ['Noun', 'Adjective'] and len(curr_word) >= 2:
            normalized = normalize_word(curr_word)
            
            # 변환된 단어가 불용어가 아니면 추가
            if normalized not in STOPWORDS:
                phrases.append(normalized)

        # --- 복합 키워드 추출 (Noun + Noun 등) ---
        # 이번에는 간단하게 '명사+명사' 조합만 추가로 봅니다.
        if i + 1 < len(pos_tags):
            next_word, next_tag = pos_tags[i+1]
            if curr_tag == 'Noun' and next_tag == 'Noun':
                if len(curr_word) > 1 and len(next_word) > 1:
                    if curr_word not in STOPWORDS and next_word not in STOPWORDS:
                        combo = f"{curr_word} {next_word}"
                        phrases.append(combo)

    return phrases

@app.route('/analyze-reviews', methods=['POST'])
def analyze_reviews():
    try:
        data = request.get_json()
        review_texts = data.get('texts', [])
        
        if not review_texts:
            return jsonify({"error": "No review texts provided"}), 400

        all_keywords = []
        
        for text in review_texts:
            extracted = extract_phrases(text)
            all_keywords.extend(extracted)
            
        # 빈도수 계산
        cnt = Counter(all_keywords)
        
        # 1. 상위 10개 후보 추출 (데이터가 적을 땐 Top 5만 뽑으면 매번 똑같음)
        top_candidates = [word for word, count in cnt.most_common(10)]
        
        # 2. 결과가 5개 미만이면 그대로 반환, 많으면 랜덤으로 섞어서 5개 반환 (다양성 확보)
        if len(top_candidates) > 5:
            final_keywords = random.sample(top_candidates, 5)
        else:
            final_keywords = top_candidates
            
        print(f"DEBUG Final Keywords: {final_keywords}") 
        
        return jsonify({
            "success": True,
            "keywords": final_keywords
        })

    except Exception as e:
        print(f"Error: {e}")
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)