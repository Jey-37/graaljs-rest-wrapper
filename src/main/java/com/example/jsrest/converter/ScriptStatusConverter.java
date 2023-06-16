package com.example.jsrest.converter;

import com.example.jsrest.model.Script;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ScriptStatusConverter implements AttributeConverter<Script.ScriptStatus, String>
{
    @Override
    public String convertToDatabaseColumn(Script.ScriptStatus scriptStatus) {
        if (scriptStatus == null)
            return null;
        return scriptStatus.name().toLowerCase();
    }

    @Override
    public Script.ScriptStatus convertToEntityAttribute(String s) {
        if (s == null)
            return null;
        return Script.ScriptStatus.valueOf(s.toUpperCase());
    }
}
