def create_summary_table(df, instance):
    df_subset = df[df['instance'] == instance] 
    summary_stats = {}

    variable_labels = {
        'time_taken': 'Execution Time (ns)',
        'total_cost': 'Total Cost',
        'total_distance': 'Total Distance',
        'f_val': 'Objective Function Value'
    }

    # Iterate over each variable to create separate summaries
    for variable, label in variable_labels.items():
        grouped = df_subset.groupby(['method'])[variable].agg(['min', 'max', 'mean']).reset_index()
        grouped.columns = ['method', 'min', 'max', 'mean']

        summary_stats[variable] = grouped

    return variable_labels, summary_stats