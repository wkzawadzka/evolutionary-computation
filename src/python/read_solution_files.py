import os
import pandas as pd

def list_previous_weeks(curr_week, directory="../../data/method_outputs/"):
    week_id = int(curr_week[1])
    entries = os.listdir(directory)
    folders = [entry for entry in entries if os.path.isdir(os.path.join(directory, entry))]
    addon_weeks = [week for week in folders if int(week[1])<week_id]
    return addon_weeks

def read_solution_files(week_name):
    base_path = f"../../data/method_outputs/{week_name}"
    if not os.path.exists(base_path):
        print(f"The directory '{base_path}' does not exist.")
        return pd.DataFrame()

    results = []  

    for method in os.listdir(base_path):
        method_path = os.path.join(base_path, method)
        
        if os.path.isdir(method_path):
            print(f"Reading files from method: {method}...")

            for instance in os.listdir(method_path):
                instance_path = os.path.join(method_path, instance)
                
                if os.path.isdir(instance_path):
                    
                    for file_name in os.listdir(instance_path):
                        if file_name.endswith(".txt"):
                            file_path = os.path.join(instance_path, file_name)
                            
                            with open(file_path, 'r') as file:
                                data = file.readlines()
                                if len(data) < 6:
                                    print(f"File {file_name} does not contain enough data.")
                                    continue

                                if method == "IteratedLocalSearch":
                                    time_taken = int(data[0].strip()) if data[0].strip().isdigit() else None
                                    total_cost = int(data[1].strip()) if data[1].strip().isdigit() else None
                                    total_distance = int(data[2].strip()) if data[2].strip().isdigit() else None
                                    f_val = int(data[3].strip()) if data[3].strip().isdigit() else None
                                    count = int(data[4].strip()) if data[4].strip().isdigit() else None

                                    solution = []
                                    for line in data[6:]:  # From line 6 onwards
                                        solution.append(int(line.strip()))

                                    result = {
                                        'method': method,
                                        'instance': instance,
                                        'time_taken': time_taken,
                                        'total_cost': total_cost,
                                        'total_distance': total_distance,
                                        'f_val': f_val,
                                        'count': count,
                                        'solution': solution
                                    }
                                    results.append(result)

                                else:
                                    # Read the last line for the solution and previous lines for metrics
                                    time_taken = int(data[0].strip()) if data[0].strip().isdigit() else None
                                    total_cost = int(data[1].strip()) if data[1].strip().isdigit() else None
                                    total_distance = int(data[2].strip()) if data[2].strip().isdigit() else None
                                    f_val = int(data[3].strip()) if data[3].strip().isdigit() else None

                                    solution = []
                                    for line in data[5:]:  # From line 6 onwards
                                        solution.append(int(line.strip()))

                                    result = {
                                        'method': method,
                                        'instance': instance,
                                        'time_taken': time_taken,
                                        'total_cost': total_cost,
                                        'total_distance': total_distance,
                                        'f_val': f_val,
                                        'solution': solution
                                    }
                                    results.append(result)

    print(f"Reading data finished\n")
    df = pd.DataFrame(results)
    return df