package com.parser.githubmining.standardization.model;

public enum ProgrammingLanguageEnum {
    JAVA("Java", ""),
    PYTHON("Python", ""),
    C("C", ""),
    C_PLUS_PLUS("C++", ""),
    C_SHARP("C#", ""),
    JAVASCRIPT("JavaScript", ""),
    RUBY("Ruby", ""),
    PHP("PHP", ""),
    GO("Go", ""),
    SWIFT("Swift", ""),
    HTML("HTML", ""),
    PLSQL("PLSQL", ""),
    JUPYTER_NOTEBOOK("Jupyter Notebook", ""),
    CSS("CSS", ""),
    NIX("Nix", ""),
    SHELL("Shell", ""),
    RUST("Rust", ""),
    SCALA("Scala", ""),
    KOTLIN("Kotlin", ""),
    WEB_ONTOLOGY("Web Ontology Language", ""),
    TYPESCRIPT("TypeScript", ""),
    OTHERS("others", "Dart,Groovy,Perl,Lua,DM...");

    private final String language;
    private final String note;

    ProgrammingLanguageEnum(String language, String note) {
        this.language = language;
        this.note = note;
    }

    public String getLanguage() {
        return language;
    }

    public String getNote() {
        return note;
    }

    @Override
    public String toString() {
        return "ProgrammingLanguageEnum{" +
                "language='" + language + '\'' +
                ", note='" + note + '\'' +
                '}';
    }

    public static ProgrammingLanguageEnum fromString(String language) {
        for (ProgrammingLanguageEnum lang : ProgrammingLanguageEnum.values()) {
            if (lang.language.equalsIgnoreCase(language)) {
                return lang;
            }
        }
        return ProgrammingLanguageEnum.OTHERS;
    }
}

