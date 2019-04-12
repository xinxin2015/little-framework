package cn.admin.core.env;


import org.junit.Test;

public class ProfilesTests {

    @Test
    public void ofSingleInvertedElement() {
        Profiles profiles = Profiles.of("!spring");
        System.out.println(profiles);
    }

}
