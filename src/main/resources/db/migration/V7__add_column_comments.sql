-- member 테이블
COMMENT ON TABLE member IS '회원 (소셜 로그인 정보 + 프로필)';
COMMENT ON COLUMN member.id IS '회원 고유 ID';
COMMENT ON COLUMN member.social_provider IS '소셜 공급자 (KAKAO, NAVER, APPLE)';
COMMENT ON COLUMN member.social_user_id IS '소셜 공급자가 부여한 사용자 고유 ID';
COMMENT ON COLUMN member.email IS '이메일 (애플은 비공개 가능, nullable)';
COMMENT ON COLUMN member.nickname IS '닉네임 (null이면 회원가입 미완료)';
COMMENT ON COLUMN member.profile_image_url IS '프로필 이미지 URL (MVP에서는 미사용)';
COMMENT ON COLUMN member.status IS '회원 상태 (ACTIVE/SUSPENDED/WITHDRAWN)';
COMMENT ON COLUMN member.created_at IS '최초 소셜 로그인 시각';
COMMENT ON COLUMN member.updated_at IS '마지막 정보 수정 시각';
COMMENT ON COLUMN member.deleted_at IS '탈퇴 시각 (null이면 활동 중)';

-- refresh_token 테이블
COMMENT ON TABLE refresh_token IS 'JWT Refresh Token (해시만 저장)';
COMMENT ON COLUMN refresh_token.id IS '토큰 고유 ID';
COMMENT ON COLUMN refresh_token.member_id IS '토큰 소유 회원 ID';
COMMENT ON COLUMN refresh_token.token_hash IS '토큰 SHA-256 해시 (원문 저장 X)';
COMMENT ON COLUMN refresh_token.expires_at IS '토큰 만료 시각';
COMMENT ON COLUMN refresh_token.revoked_at IS '토큰 폐기 시각 (null이면 유효)';
COMMENT ON COLUMN refresh_token.created_at IS '토큰 발급 시각';

-- departure_place 테이블
COMMENT ON TABLE departure_place IS '출발지 (회원당 최대 10개)';
COMMENT ON COLUMN departure_place.id IS '출발지 고유 ID';
COMMENT ON COLUMN departure_place.member_id IS '소유 회원 ID';
COMMENT ON COLUMN departure_place.label IS '라벨 (집, 회사 등)';
COMMENT ON COLUMN departure_place.address IS '주소 원문';
COMMENT ON COLUMN departure_place.latitude IS '위도';
COMMENT ON COLUMN departure_place.longitude IS '경도';
COMMENT ON COLUMN departure_place.geo_point IS 'PostGIS 좌표 (GEOGRAPHY POINT)';
COMMENT ON COLUMN departure_place.is_default IS '기본 출발지 여부 (회원당 1개만 true)';
COMMENT ON COLUMN departure_place.created_at IS '등록 시각';
COMMENT ON COLUMN departure_place.updated_at IS '수정 시각';

-- terms 테이블
COMMENT ON TABLE terms IS '약관 (유형별 버전 관리)';
COMMENT ON COLUMN terms.id IS '약관 고유 ID';
COMMENT ON COLUMN terms.type IS '약관 유형 (TERMS_OF_SERVICE/PRIVACY_POLICY/MARKETING)';
COMMENT ON COLUMN terms.version IS '약관 버전 (예: 1.0)';
COMMENT ON COLUMN terms.title IS '약관 제목';
COMMENT ON COLUMN terms.content IS '약관 본문';
COMMENT ON COLUMN terms.is_required IS '필수 동의 여부 (true면 미동의 시 가입 불가)';
COMMENT ON COLUMN terms.effective_from IS '약관 시행일';
COMMENT ON COLUMN terms.created_at IS '등록 시각';

-- terms_agreement 테이블
COMMENT ON TABLE terms_agreement IS '약관 동의 이력 (DELETE 금지 - 법적 증적)';
COMMENT ON COLUMN terms_agreement.id IS '동의 이력 고유 ID';
COMMENT ON COLUMN terms_agreement.member_id IS '동의한 회원 ID';
COMMENT ON COLUMN terms_agreement.terms_id IS '동의한 약관 ID';
COMMENT ON COLUMN terms_agreement.agreed_at IS '동의 시각';

-- device_token 테이블
COMMENT ON TABLE device_token IS 'iOS 디바이스 토큰 (푸시 알림용, 발송은 후순위)';
COMMENT ON COLUMN device_token.id IS '토큰 고유 ID';
COMMENT ON COLUMN device_token.member_id IS '소유 회원 ID';
COMMENT ON COLUMN device_token.token IS 'APNs 디바이스 토큰';
COMMENT ON COLUMN device_token.platform IS '플랫폼 (IOS)';
COMMENT ON COLUMN device_token.app_version IS '앱 버전';
COMMENT ON COLUMN device_token.created_at IS '등록 시각';
COMMENT ON COLUMN device_token.updated_at IS '갱신 시각';
