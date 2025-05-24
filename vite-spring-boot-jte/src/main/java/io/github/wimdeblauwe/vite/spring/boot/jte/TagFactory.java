package io.github.wimdeblauwe.vite.spring.boot.jte;

class TagFactory {
    public static String generateScriptTag(final String src) {
        return "<script type=\"module\" src=\"%s\"></script>%n".formatted(src);
    }

    public static String generateScriptTagWithContent(final String content) {
        return "<script type=\"module\">%s</script>%n".formatted(content);
    }

    public static String generateCssLinkTag(final String href) {
        return "<link rel=\"stylesheet\" href=\"%s\"/>%n".formatted(href);
    }
}
