package com.example.nasda.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // 1. "이 파일은 설정 파일이다"라고 선언함
public class ModelMapperConfig {

    @Bean // 2. "이 기계(ModelMapper)를 스프링 상자에 등록한다"는 뜻
    public ModelMapper getMapper() {

        ModelMapper modelMapper = new ModelMapper(); // 기계 조립 시작

        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true) // 3. 이름이 같으면 알아서 매칭해라
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE) // 4. 잠긴(private) 데이터도 열어라
                .setMatchingStrategy(MatchingStrategies.STRICT); // 5. [가장 중요] 이름이 100% 똑같아야만 옮겨라 (배달 사고 방지)

        return modelMapper; // 완성된 기계 반환
    }
}