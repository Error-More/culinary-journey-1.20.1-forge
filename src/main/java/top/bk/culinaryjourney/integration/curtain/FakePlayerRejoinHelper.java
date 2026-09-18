package top.bk.culinaryjourney.integration.curtain;

//Copy from https://github.com/TISUnion/Carpet-TIS-Addition/blob/master/src/main/java/carpettisaddition/helpers/carpet/tweaks/command/fakePlayerRejoin/FakePlayerRejoinHelper.java

public class FakePlayerRejoinHelper
{
    public static final ThreadLocal<Boolean> isRejoin = ThreadLocal.withInitial(() -> false);
}