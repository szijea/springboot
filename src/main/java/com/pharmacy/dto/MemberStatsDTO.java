package com.pharmacy.dto;

public class MemberStatsDTO {
    private Long totalMembers;
    private Long vipMembers;
    private Long newMembers;
    private Long todayConsumption;
    private Double totalConsumption;
    private Long sleepingMembers;

    // 增长率或变化量
    private Integer totalGrowth;
    private Integer vipGrowth;
    private Integer newGrowth;
    private Integer todayGrowth;
    private Double consumptionGrowth;
    private Integer sleepingGrowth;

    public MemberStatsDTO() {}

    // Getter 和 Setter 方法
    public Long getTotalMembers() { return totalMembers; }
    public void setTotalMembers(Long totalMembers) { this.totalMembers = totalMembers; }

    public Long getVipMembers() { return vipMembers; }
    public void setVipMembers(Long vipMembers) { this.vipMembers = vipMembers; }

    public Long getNewMembers() { return newMembers; }
    public void setNewMembers(Long newMembers) { this.newMembers = newMembers; }

    public Long getTodayConsumption() { return todayConsumption; }
    public void setTodayConsumption(Long todayConsumption) { this.todayConsumption = todayConsumption; }

    public Double getTotalConsumption() { return totalConsumption; }
    public void setTotalConsumption(Double totalConsumption) { this.totalConsumption = totalConsumption; }

    public Long getSleepingMembers() { return sleepingMembers; }
    public void setSleepingMembers(Long sleepingMembers) { this.sleepingMembers = sleepingMembers; }

    public Integer getTotalGrowth() { return totalGrowth; }
    public void setTotalGrowth(Integer totalGrowth) { this.totalGrowth = totalGrowth; }

    public Integer getVipGrowth() { return vipGrowth; }
    public void setVipGrowth(Integer vipGrowth) { this.vipGrowth = vipGrowth; }

    public Integer getNewGrowth() { return newGrowth; }
    public void setNewGrowth(Integer newGrowth) { this.newGrowth = newGrowth; }

    public Integer getTodayGrowth() { return todayGrowth; }
    public void setTodayGrowth(Integer todayGrowth) { this.todayGrowth = todayGrowth; }

    public Double getConsumptionGrowth() { return consumptionGrowth; }
    public void setConsumptionGrowth(Double consumptionGrowth) { this.consumptionGrowth = consumptionGrowth; }

    public Integer getSleepingGrowth() { return sleepingGrowth; }
    public void setSleepingGrowth(Integer sleepingGrowth) { this.sleepingGrowth = sleepingGrowth; }
}